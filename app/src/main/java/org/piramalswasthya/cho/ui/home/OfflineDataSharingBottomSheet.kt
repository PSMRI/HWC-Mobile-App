package org.piramalswasthya.cho.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOfflineDataSharingBottomSheetBinding
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.utils.Constants
import timber.log.Timber


class OfflineDataSharingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentOfflineDataSharingBottomSheetBinding?= null
    private val binding:FragmentOfflineDataSharingBottomSheetBinding
        get()= _binding!!

    private val viewModel: OfflineSyncViewModel by viewModels({requireActivity()})
    private lateinit var connectionsClient: ConnectionsClient
    private val discoveredDevices = mutableListOf<String>()
    private val adapter by lazy { ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, discoveredDevices) }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        connectionsClient = (requireActivity() as HomeActivity).connectionsClient
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOfflineDataSharingBottomSheetBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setObservers()
        setListeners()
    }

    private fun initView() {
        binding.listViewDevices.adapter = adapter
    }

    private fun setListeners() {
        binding.findDevicesBtn.setOnClickListener {
//            binding.listViewDevices.setOnItemClickListener { _, _, position, _ ->
//                val endpointId = endpointIds[position]
//                requestConnection(endpointId)
//            }
            binding.apply {
                findDevicesBtn.text = "Cancel"
                availableDevices.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                listViewDevices.visibility = View.GONE

            }
            connectionsClient.stopDiscovery()
            startDiscovery()
        }
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

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(
            Constants.serviceId, endpointDiscoveryCallback, discoveryOptions
        ).addOnSuccessListener {
            Log.d("Discovery","Discovery started")
        }.addOnFailureListener {
            Log.d("Discovery","Discovery failed ${it.message}")
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {


              if (info.endpointName.contains("Nurse")) {
                  Log.d("Discovery","Device found ${info.endpointName}")
                  if(!discoveredDevices.contains(info.endpointName)) {
                      discoveredDevices.add(info.endpointName)
                      adapter.notifyDataSetChanged()
                  }
                  binding.apply {
                      findDevicesBtn.text = "Find Available Devices"
                      availableDevices.visibility = View.VISIBLE
                      progressBar.visibility = View.GONE
                      listViewDevices.visibility = View.VISIBLE

                  }
              }

        }

        override fun onEndpointLost(p0: String) {

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