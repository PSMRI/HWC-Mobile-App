package org.piramalswasthya.cho.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentOfflineDataSharingBottomSheetBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.Constants
import org.piramalswasthya.cho.utils.DateJsonAdapter
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class OfflineDataSharingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentOfflineDataSharingBottomSheetBinding? = null
    private val binding: FragmentOfflineDataSharingBottomSheetBinding
        get() = _binding!!

    @Inject
    lateinit var patientDao: PatientDao

    private val viewModel: OfflineSyncViewModel by viewModels({ requireActivity() })
    private lateinit var connectionsClient: ConnectionsClient
    private val endpointIdToDeviceNameMap = mutableMapOf<String, String>()
    private val discoveredDevices = mutableListOf<String>()
    private val adapter by lazy {
        ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            discoveredDevices
        )
    }
    private lateinit var progressDialog: ProgressDialogFragment


    override fun onAttach(context: Context) {
        super.onAttach(context)
        connectionsClient = (requireActivity() as HomeActivity).connectionsClient
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialogFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentOfflineDataSharingBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setObservers()
        initView()
        setListeners()
    }

    private fun initView() {
        binding.listViewDevices.adapter = adapter
        binding.progressBar.visibility = View.VISIBLE
        binding.listViewDevices.visibility = View.GONE
        connectionsClient.stopDiscovery()
        startDiscovery()
    }

    private fun setObservers() {
        OfflineSyncViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                OfflineSyncViewModel.State.IDLE -> {

                }

                OfflineSyncViewModel.State.FETCHING -> {

                }

                OfflineSyncViewModel.State.SUCCESS -> {
                    if (viewModel.userRole == "Registrar") {
                        binding.unsyncedRecordsCount.text = viewModel.patients.size.toString()
                        Timber.d("count ${viewModel.patients}")
                    }
                }

                OfflineSyncViewModel.State.FAILED -> {

                }
            }

        }
    }

    private fun setListeners() {
        binding.listViewDevices.setOnItemClickListener { adapterView, view, position, l ->
            val deviceName = discoveredDevices[position]
            var endpointId = ""
            for ((key, mapValue) in endpointIdToDeviceNameMap) {
                if (mapValue == deviceName) {
                    endpointId = key
                }
            }
            Log.d("Discovery", " $endpointId $deviceName")
            progressDialog.show(childFragmentManager, "progressDialog")
            requestConnection(endpointId!!, deviceName)
        }
    }

    private fun requestConnection(endpointId: String, deviceName: String) {
        connectionsClient.requestConnection(buildString {
            append(viewModel.userRole)
            append("-")
            append(viewModel.userName)
        }, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener {
                Log.d("Discovery", "Connection request sent to: $endpointId $deviceName from ${viewModel.userName} of role ${viewModel.userRole}")

                progressDialog.updateUI("Connecting...")
            }.addOnFailureListener {
                Log.e("Discovery", "Failed to send connection request to: $endpointId $deviceName")
            }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d("Discovery", "Connection initiated with: $endpointId")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d("Discovery", "Connected to: $endpointId")
                    //send data
                    progressDialog.updateUI("Connected!")
                    if(viewModel.patients.isNotEmpty()) {
                        sendPayload(endpointId, viewModel.patients)
                    }else{
                        Toast.makeText(requireContext(), "No patients to sync", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("Discovery", "Connection rejected by: $endpointId")
                    progressDialog.updateUI("Connection rejected!")

                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e("Discovery", "Connection error with: $endpointId")
                    progressDialog.updateUI("Unable to connect!")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("Discovery", "Disconnected from: $endpointId")
        }

    }

    fun sendPayload(endpointId: String, patients: List<PatientDisplay>) {
        //for each item in list use moshi to serilaize only the patient and send that to nurse
        val moshi =  Moshi.Builder()
            .add(DateJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        val patientAdapter = moshi.adapter(Patient::class.java)
       // progressDialog.show(childFragmentManager, "progressDialog")

        CoroutineScope(Dispatchers.IO).launch {
            var filesSent = 0
            val totalFiles = patients.size

            for (patientDisplay in patients) {
                try {
                    patientDao.updatePatientSyncing(SyncState.SYNCING,patientDisplay.patient.patientID)

                    // Serialize the Patient object to JSON

                    val patientJson = patientAdapter.toJson(patientDisplay.patient)

                    // Create a Payload from the JSON string
                    val payload = Payload.fromBytes(patientJson.toByteArray())

                    // Send the Payload to the specified endpoint
                    connectionsClient.sendPayload(endpointId, payload)
                    patientDao.updatePatientSyncOffline(SyncState.SHARED_OFFLINE,patientDisplay.patient.patientID)
                    // Update the progress on the main thread
                    withContext(Dispatchers.Main) {
                        filesSent++
                        val progress = (filesSent * 100 / totalFiles)
                        progressDialog.updateProgress(filesSent, totalFiles, progress)
                        // updateProgress(filesSent, totalFiles)
                    }
                } catch (e: Exception) {
                    patientDao.updatePatientSyncFailed(SyncState.UNSYNCED, patientDisplay.patient.patientID)
                    e.printStackTrace()
                    // Handle serialization or sending errors
                }
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.IN_PROGRESS) {
                val progress = (update.bytesTransferred * 100 / update.totalBytes).toInt()
                // updateProgress(progress)
            } else if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                // Handle successful transfer if needed
                progressDialog.updateUI("Data sent successfully!")
                // Update unsynced records count
                viewModel.getUnsyncedRegistrarData()

                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                }, 2000)
//                progressDialog.dismiss()
//                viewModel.getUnsyncedRegistrarData()
            } else if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                // Handle failed transfer if needed
                progressDialog.updateUI("Failed to send data.")
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                }, 2000)

            }
        }

    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(
            Constants.serviceId, endpointDiscoveryCallback, discoveryOptions
        ).addOnSuccessListener {
            Log.d("Discovery", "Discovery started")
        }.addOnFailureListener {
            Log.d("Discovery", "Discovery failed ${it.message}")
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {

            if (viewModel.userRole == "Registrar") {
                if (info.endpointName.contains("Nurse")) {
                    Log.d("Discovery", "Device found ${info.endpointName}")
                    if (!discoveredDevices.contains(info.endpointName)) {
                        discoveredDevices.add(info.endpointName)
                        endpointIdToDeviceNameMap[endpointId] = info.endpointName
                        adapter.notifyDataSetChanged()
                    }
                    binding.apply {
                        progressBar.visibility = View.GONE
                        listViewDevices.visibility = View.VISIBLE
                    }
                }
            } else if (viewModel.userRole == "Nurse") {

            }
        }

        override fun onEndpointLost(endpointId: String) {
            //REMOVE THAT DEVICE FROM THE LIST
            val deviceName = endpointIdToDeviceNameMap.remove(endpointId)
            if (deviceName != null) {
                discoveredDevices.remove(deviceName)
                adapter.notifyDataSetChanged()
            }
            if (discoveredDevices.isEmpty()) {
                binding.apply {
                    progressBar.visibility = View.VISIBLE
                    listViewDevices.visibility = View.GONE
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        connectionsClient.stopDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    enum class Role {
        REGISTRAR, NURSE, DOCTOR, LAB_TECHNICIAN, PHARMACIST
    }

    fun getNextRole(currentRole: Role): Role? {
        return when (currentRole) {
            Role.REGISTRAR -> Role.NURSE
            Role.NURSE -> Role.DOCTOR
            Role.DOCTOR -> Role.LAB_TECHNICIAN
            Role.LAB_TECHNICIAN -> Role.PHARMACIST
            Role.PHARMACIST -> null // End of chain
        }
    }


}