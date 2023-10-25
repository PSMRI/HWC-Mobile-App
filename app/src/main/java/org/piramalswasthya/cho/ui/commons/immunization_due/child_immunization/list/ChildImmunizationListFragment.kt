//package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.launch
//import org.piramalswasthya.sakhi.R
//import org.piramalswasthya.sakhi.adapters.ImmunizationBenListAdapter
//import org.piramalswasthya.sakhi.databinding.FragmentChildImmunizationListBinding
//import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
//import timber.log.Timber
//
//@AndroidEntryPoint
//class ChildImmunizationListFragment : Fragment() {
//
//    private var _binding: FragmentChildImmunizationListBinding? = null
//    private val binding: FragmentChildImmunizationListBinding
//        get() = _binding!!
//
//
//    private val viewModel: ChildImmunizationListViewModel by viewModels()
//
//    private val bottomSheet: ChildImmunizationVaccineBottomSheetFragment by lazy { ChildImmunizationVaccineBottomSheetFragment() }
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentChildImmunizationListBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.rvList.adapter =
//            ImmunizationBenListAdapter(ImmunizationBenListAdapter.VaccinesClickListener {
//                viewModel.updateBottomSheetData(it)
//                if (!bottomSheet.isVisible)
//                    bottomSheet.show(childFragmentManager, "ImM")
//            })
//
//        lifecycleScope.launch {
//            viewModel.benWithVaccineDetails.collect {
//                Timber.d("Collecting list : $it")
//                binding.rvList.apply {
//                    (adapter as ImmunizationBenListAdapter).submitList(it)
//                }
//            }
//        }
//
//
////        bottomSheet.setContentFlow(viewModel.bottomSheetContent)
////        lifecycleScope.launch {
////            viewModel.bottomSheetContent.collect {
////                Timber.d("Collecting list : $it")
////                it?.let { bottomSheet.submitListToVaccinationRv(it) }
////            }
////        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        activity?.let {
//            (it as HomeActivity).updateActionBar(
//                R.drawable.ic__immunization,
//                getString(R.string.child_immunization_list)
//            )
//        }
//    }
//
//
//}