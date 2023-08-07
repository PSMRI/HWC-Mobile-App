package org.piramalswasthya.cho.ui.commons.govt_id

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentGovtIdBinding
import org.piramalswasthya.cho.databinding.FragmentOtherInformationsBinding
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.repositories.GovIdEntityMasterRepo
import javax.inject.Inject

@AndroidEntryPoint
class GovtIdFragment constructor(private val fragmentTag: String, private val linearLayout: LinearLayout, private var fragmentTagList: ArrayList<String>, private val button: Button,): Fragment() {

    companion object {

    }

    private lateinit var viewModel: GovtIdViewModel

    @Inject
    lateinit var govIdEntityMasterRepo: GovIdEntityMasterRepo

    private var _binding: FragmentGovtIdBinding? = null

    private val binding: FragmentGovtIdBinding
        get() = _binding!!

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var selectedGovtIdType : GovIdEntityMaster? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGovtIdBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GovtIdViewModel::class.java)

        linearLayout.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            binding.deleteButton.isEnabled = linearLayout.childCount > 1
        }


        binding.deleteButton.setOnClickListener {
            if(linearLayout.childCount > 1){
                val fragmentToRemove = requireActivity().supportFragmentManager.findFragmentByTag(fragmentTag)
                fragmentToRemove?.let {
                    requireActivity().supportFragmentManager.beginTransaction().remove(it).commit()
                }
                fragmentTagList.remove(fragmentTag)
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fetchGovtIds()
    }

    private fun fetchGovtIds(){
        coroutineScope.launch {
            val govtIdMap = govIdEntityMasterRepo.getGovIdtEntityAsMap()
            if(govtIdMap != null){
                val govtIdNames = govtIdMap.values.toTypedArray()
                binding.dropdownGovtIdType.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, govtIdNames))
                if(govtIdNames.isNotEmpty()) {
                    selectedGovtIdType = GovIdEntityMaster( govtIdMap!!.entries.toList()[0].key, govtIdMap!!.entries.toList()[0].value)
                    binding.dropdownGovtIdType.setText(selectedGovtIdType!!.identityType, false)
                }
                binding.dropdownGovtIdType.setOnItemClickListener { parent, _, position, _ ->
                    selectedGovtIdType = GovIdEntityMaster( govtIdMap!!.entries.toList()[position].key, govtIdMap!!.entries.toList()[position].value)
                    binding.dropdownGovtIdType.setText(selectedGovtIdType!!.identityType, false)
                }
            }
        }
    }

}