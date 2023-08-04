package org.piramalswasthya.cho.ui.commons.govt_health_prog

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentGovtHealthProgBinding
import org.piramalswasthya.cho.databinding.FragmentGovtIdBinding
import org.piramalswasthya.cho.databinding.FragmentOtherInformationsBinding
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.repositories.GovIdEntityMasterRepo
import org.piramalswasthya.cho.repositories.OtherGovIdEntityMasterRepo
import javax.inject.Inject

@AndroidEntryPoint
class GovtHealthProgFragment : Fragment() {

    companion object {

    }

    private lateinit var viewModel: GovtHealthProgViewModel

    @Inject
    lateinit var otherGovIdEntityMasterRepo: OtherGovIdEntityMasterRepo

    private var _binding: FragmentGovtHealthProgBinding? = null

    private val binding: FragmentGovtHealthProgBinding
        get() = _binding!!

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var selectedGovtHealthProgType : OtherGovIdEntityMaster? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGovtHealthProgBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GovtHealthProgViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fetchOtherGovtIds()
    }

    private fun fetchOtherGovtIds(){
        coroutineScope.launch {
            val otherGovtIdMap = otherGovIdEntityMasterRepo.getOtherGovtEntityAsMap()
            if(otherGovtIdMap != null){
                val otherGovtIdNames = otherGovtIdMap.values.toTypedArray()
                binding.dropdownGovtHealthProgType.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, otherGovtIdNames))
                if(otherGovtIdNames.isNotEmpty()) {
                    selectedGovtHealthProgType = OtherGovIdEntityMaster(otherGovtIdMap!!.entries.toList()[0].key, otherGovtIdMap!!.entries.toList()[0].value)
                    binding.dropdownGovtHealthProgType.setText(selectedGovtHealthProgType!!.identityType, false)
                }
                binding.dropdownGovtHealthProgType.setOnItemClickListener { parent, _, position, _ ->
                    selectedGovtHealthProgType = OtherGovIdEntityMaster(otherGovtIdMap!!.entries.toList()[position].key, otherGovtIdMap!!.entries.toList()[position].value)
                    binding.dropdownGovtHealthProgType.setText(selectedGovtHealthProgType!!.identityType, false)
                }
            }

        }
    }

}