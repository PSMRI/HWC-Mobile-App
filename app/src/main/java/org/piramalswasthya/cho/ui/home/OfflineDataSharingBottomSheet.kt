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
import androidx.lifecycle.ViewModelProvider
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
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.FragmentOfflineDataSharingBottomSheetBinding
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDoctorBundle
import org.piramalswasthya.cho.model.PatientVisitDataBundle
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PayloadWrapper
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.Constants
import org.piramalswasthya.cho.utils.DateJsonAdapter
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume


@AndroidEntryPoint
class OfflineDataSharingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentOfflineDataSharingBottomSheetBinding? = null
    private val binding: FragmentOfflineDataSharingBottomSheetBinding
        get() = _binding!!

    @Inject
    lateinit var patientDao: PatientDao

    private lateinit var  viewModel: OfflineSyncViewModel
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
        viewModel = ViewModelProvider(requireActivity()).get(OfflineSyncViewModel::class.java)
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
                    } else if (viewModel.userRole == "Nurse") {
                        binding.unsyncedRecordsCount.text =
                            viewModel.patientVisitDataBundle.size.toString()
                    }else if(viewModel.userRole == "Doctor"){
                        binding.unsyncedRecordsCount.text = viewModel.patientDoctorBundle.size.toString()
                    }else if(viewModel.userRole == "Registrar&Nurse"){
                        binding.unsyncedRecordsCount.text =
                            viewModel.patientVisitDataBundle.size.toString()
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
                Log.d(
                    "Discovery",
                    "Connection request sent to: $endpointId $deviceName from ${viewModel.userName} of role ${viewModel.userRole}"
                )

                progressDialog.updateUI("Connecting...")
            }.addOnFailureListener {
                progressDialog.updateUI("Failed to send connection request")
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                }, 2000)
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
                    if (viewModel.userRole == "Registrar") {
                        if (viewModel.patients.isNotEmpty()) {
                            sendPayload(endpointId, viewModel.patients) { success ->
                                if (!success) {
                                    progressDialog.updateUI("Failed to send Registrar Data")
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No patients to sync",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else if (viewModel.userRole == "Nurse") {
                        sendNurseToDoctorPayload(endpointId, viewModel.patientVisitDataBundle)
                    }else if(viewModel.userRole == "Doctor"){
                        sendDoctorToPharmacistPayload(endpointId, viewModel.patientDoctorBundle)
                    }else if(viewModel.userRole=="Registrar&Nurse"){
                        sendNurseToDoctorPayload(endpointId, viewModel.patientVisitDataBundle)
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

    private fun sendDoctorToPharmacistPayload(
        endpointId: String,
        patientDoctorBundle: MutableList<PatientDoctorBundle>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            progressDialog.updateUI("Sending Doctor data..")
            Log.d("Discovery", "Sending Doctor data to Pharmacist")

            val moshi = Moshi.Builder()
                .add(DateJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
            val patientDoctorBundleAdapter = moshi.adapter(PatientDoctorBundle::class.java)
            val payloadWrapperAdapter = moshi.adapter(PayloadWrapper::class.java)
            var filesSent = 0
            val totalFiles = patientDoctorBundle.size
            for (data in patientDoctorBundle){
                try{
                   // viewModel.patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSyncing(data.patient.patientID, data.patientVisitInfoSync.benVisitNo)
                    val patientDoctorBundleJson = patientDoctorBundleAdapter.toJson(data)
                    val payloadWrapper = PayloadWrapper(
                        type = "PatientDoctorBundle",
                        data = patientDoctorBundleJson
                    )
                    val payloadWrapperJson = payloadWrapperAdapter.toJson(payloadWrapper)

                    // Create a Payload from the JSON string
                    val payload = Payload.fromBytes(payloadWrapperJson.toByteArray())
                    // Send the Payload to the specified endpoint
                    connectionsClient.sendPayload(endpointId, payload)
                   // viewModel.patientVisitInfoSyncRepo.updatePatientDoctorDataSyncOffline(data.patient.patientID, data.patientVisitInfoSync.benVisitNo)

                    Log.d("Discovery", "Sending Doctor data: $filesSent")

                    withContext(Dispatchers.Main) {
                        filesSent++
                        val progress = (filesSent * 100 / totalFiles)
                        progressDialog.updateProgress(filesSent, totalFiles, progress)
                        // updateProgress(filesSent, totalFiles)
                    }
                } catch (e: Exception) {
                    viewModel.patientVisitInfoSyncRepo.updatePatientDoctorDataSyncFailed(data.patient.patientID, data.patientVisitInfoSync.benVisitNo)
                    Log.d("Discovery", "Sending Doctor data error: ${e.message}")
                    e.printStackTrace()
                }
            }
            withContext(Dispatchers.Main){
                if(filesSent == totalFiles){
                    progressDialog.updateUI("Doctor data sent Sucessfully")
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                    }, 2000)
                    connectionsClient.stopAllEndpoints()
                    viewModel.patientDoctorBundle.clear()
                    viewModel.getUnsyncedDoctorData()
                }
            }

        }
    }

    private fun sendNurseToDoctorPayload(
        endpointId: String,
        patientVisitDataBundle: MutableList<PatientVisitDataBundle>
    ) {
        progressDialog.updateUI("Sending Registrar data..")
        Log.d("Discovery", "Sending Registrar data")

        CoroutineScope(Dispatchers.IO).launch {

            var success = true
            if (success) {
                progressDialog.updateUI("Sending Nurse data..")
                Log.d("Discovery", "Sending Nurse data")

                val moshi = Moshi.Builder()
                    .add(DateJsonAdapter())
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val patientVisitDataBundleAdapter =
                    moshi.adapter(PatientVisitDataBundle::class.java)
                val payloadWrapperAdapter = moshi
                    .adapter(PayloadWrapper::class.java)
                var filesSent = 0
                val totalFiles = patientVisitDataBundle.size
                for (data in patientVisitDataBundle) {
                    try {
                        viewModel.patientVisitInfoSyncRepo.updatePatientNurseDataSyncSyncing(
                            data.patientVisitInfoSync.patientID,
                            data.patientVisitInfoSync.benVisitNo
                        )

                        // Serialize the Patient object to JSON

                        val patientVisitDataBundleJson = patientVisitDataBundleAdapter.toJson(data)
                        val payloadWrapper = PayloadWrapper(
                            type = "PatientVisitDataBundle",
                            data = patientVisitDataBundleJson
                        )
                        val payloadWrapperJson = payloadWrapperAdapter.toJson(payloadWrapper)

                        // Create a Payload from the JSON string
                        val payload = Payload.fromBytes(payloadWrapperJson.toByteArray())

                        // Send the Payload to the specified endpoint
                        connectionsClient.sendPayload(endpointId, payload)
                        viewModel.patientVisitInfoSyncRepo.updatePatientNurseDataOfflineSyncSuccess(
                            data.patientVisitInfoSync.patientID,
                            data.patientVisitInfoSync.benVisitNo
                        )
                        patientDao.updatePatientSyncOffline(
                            SyncState.SHARED_OFFLINE,
                            data.patientVisitInfoSync.patientID
                        )
                        Log.d("Discovery", "Sending Registrar data: $filesSent")

                        withContext(Dispatchers.Main) {
                            filesSent++
                            val progress = (filesSent * 100 / totalFiles)
                            progressDialog.updateProgress(filesSent, totalFiles, progress)
                            // updateProgress(filesSent, totalFiles)
                        }
                    } catch (e: Exception) {
                        viewModel.patientVisitInfoSyncRepo.updatePatientNurseDataSyncFailed(
                            data.patientVisitInfoSync.patientID,
                            data.patientVisitInfoSync.benVisitNo
                        )
                        Log.d("Discovery", "Sending Nurse data error: ${e.message}")

                        e.printStackTrace()
                    }

                }
                withContext(Dispatchers.Main){
                    if(filesSent == totalFiles){
                        progressDialog.updateUI("Nurse data sent Sucessfully")
                        Handler(Looper.getMainLooper()).postDelayed({
                            progressDialog.dismiss()
                        }, 2000)
                        connectionsClient.stopAllEndpoints()
                        viewModel.patientVisitDataBundle.clear()
                        viewModel.getUnsyncedNurseData()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    progressDialog.updateUI("Failed to send Registrar data.")
                }
            }


        }
    }

    fun sendPayload(
        endpointId: String,
        patients: List<PatientDisplay>,
        onComplete: (Boolean) -> Unit
    ) {
        //for each item in list use moshi to serilaize only the patient and send that to nurse
        val moshi = Moshi.Builder()
            .add(DateJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        val patientAdapter = moshi.adapter(Patient::class.java)
        val payloadWrapperAdapter = moshi.adapter(PayloadWrapper::class.java)
        // progressDialog.show(childFragmentManager, "progressDialog")

        CoroutineScope(Dispatchers.IO).launch {
            var filesSent = 0
            val totalFiles = patients.size
            var success = true

            for (patientDisplay in patients) {
                try {
                    patientDao.updatePatientSyncing(
                        SyncState.SYNCING,
                        patientDisplay.patient.patientID
                    )

                    // Serialize the Patient object to JSON

                    val patientJson = patientAdapter.toJson(patientDisplay.patient)
                    val payloadWrapper = PayloadWrapper(type = "Patient", data = patientJson)
                    val payloadWrapperJson = payloadWrapperAdapter.toJson(payloadWrapper)
                    // Create a Payload from the JSON string
                    val payload = Payload.fromBytes(payloadWrapperJson.toByteArray())

                    // Send the Payload to the specified endpoint
                    connectionsClient.sendPayload(endpointId, payload)
                    patientDao.updatePatientSyncOffline(
                        SyncState.SHARED_OFFLINE,
                        patientDisplay.patient.patientID
                    )
                    Log.d("Discovery", "No. of Reg data sent: $filesSent")

                    // Update the progress on the main thread
                    withContext(Dispatchers.Main) {
                        filesSent++
                        val progress = (filesSent * 100 / totalFiles)
                        progressDialog.updateProgress(filesSent, totalFiles, progress)
                        // updateProgress(filesSent, totalFiles)
                    }
                } catch (e: Exception) {
                    patientDao.updatePatientSyncFailed(
                        SyncState.UNSYNCED,
                        patientDisplay.patient.patientID
                    )
                    e.printStackTrace()
                    success = false
                    break
                    // Handle serialization or sending errors
                }
            }
            withContext(Dispatchers.Main) {
                if (filesSent == totalFiles) {
                    progressDialog.updateUI("Registrar Data sent successfully!")
                    viewModel.getUnsyncedRegistrarData()
                    if(viewModel.userRole == "Registrar") {
                        Handler(Looper.getMainLooper()).postDelayed({
                            progressDialog.dismiss()
                        }, 2000)
                        connectionsClient.stopAllEndpoints()
                    }
                }
                onComplete(success)
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

            } else if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                // Handle failed transfer if needed
                progressDialog.updateUI("Failed to send data.")
                connectionsClient.stopAllEndpoints()
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
            val roleToDeviceMap = mapOf(
                "Registrar" to "Nurse",
                "Nurse" to "MO",
                "Registrar&Nurse" to "MO",
                "MO" to "Pharmacist",
                "Doctor" to "Pharmacist",
            )

            val targetDeviceName = roleToDeviceMap[viewModel.userRole]

            if (targetDeviceName != null && info.endpointName.contains(targetDeviceName)) {
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