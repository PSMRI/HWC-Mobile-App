package org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.ImmunizationCategoryAdapter
import org.piramalswasthya.cho.databinding.BottomSheetImmVaccineBinding
import org.piramalswasthya.cho.model.ChildImmunizationCategory
import org.piramalswasthya.cho.model.ImmunizationDetailsDomain
import org.piramalswasthya.cho.model.VaccineCategoryDomain
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FragmentVisitDetailDirections
import timber.log.Timber

@AndroidEntryPoint
class ChildImmunizationVaccineBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetImmVaccineBinding? = null
    private val binding: BottomSheetImmVaccineBinding
        get() = _binding!!

    private val viewModel: ChildImmunizationListViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetImmVaccineBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvImmCat.adapter =
            ImmunizationCategoryAdapter(ImmunizationCategoryAdapter.ImmunizationIconClickListener {
                val benId = viewModel.getSelectedBenId()
                findNavController().navigate(
                    FragmentVisitDetailDirections.actionFhirVisitDetailsFragmentToImmunizationFormFragment(
                        benId, it
                    )
                )
                dismiss()
            })

        lifecycleScope.launch {
            viewModel.bottomSheetContent.collect {
                it?.let {
                    submitListToVaccinationRv(it)
                }
            }
        }
    }


    private fun submitListToVaccinationRv(detail: ImmunizationDetailsDomain) {
        val list = ChildImmunizationCategory.values().map { category ->
            VaccineCategoryDomain(category,
                vaccineStateList = detail.vaccineStateList.filter { it.vaccineCategory == category })
        }.filter { it.vaccineStateList.isNotEmpty() }
        Timber.d("Called list at bottom sheet ${_binding?.rvImmCat?.adapter} ${detail.ben.patientID} $list")
        (_binding?.rvImmCat?.adapter as ImmunizationCategoryAdapter?)?.submitList(list)
    }

    override fun dismiss() {
//        submitListToAncRv(emptyList())
        super.dismiss()
    }

    fun setContentFlow(bottomSheetContent: Flow<ImmunizationDetailsDomain?>) {
        lifecycleScope.launchWhenResumed {
            findNavController().currentBackStackEntry
            bottomSheetContent.collect {
                it?.let { it1 -> submitListToVaccinationRv(it1) }
            }
        }
    }

}