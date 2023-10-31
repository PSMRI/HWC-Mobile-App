//package org.piramalswasthya.cho.ui.commons.immunization_due.child_immunization.categories
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.GridLayoutManager
//import dagger.hilt.android.AndroidEntryPoint
//import org.piramalswasthya.sakhi.R
//import org.piramalswasthya.sakhi.adapters.IconGridAdapter
//import org.piramalswasthya.sakhi.configuration.IconDataset
//import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
//import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class ChildImmunizationCategoriesFragment : Fragment() {
//
//    @Inject
//    lateinit var iconDataset: IconDataset
//
//
//    private var _binding: RvIconGridBinding? = null
//    private val binding: RvIconGridBinding
//        get() = _binding!!
//
//    private val viewModel: ChildImmunizationCategoriesViewModel by viewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = RvIconGridBinding.inflate(layoutInflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setUpCategoriesIconRvAdapter()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        activity?.let {
//            (it as HomeActivity).updateActionBar(R.drawable.ic__child)
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null
//    }
//
//
//    private fun setUpCategoriesIconRvAdapter() {
//        val rvLayoutManager = GridLayoutManager(
//            context,
//            requireContext().resources.getInteger(R.integer.icon_grid_span)
//        )
//        binding.rvIconGrid.layoutManager = rvLayoutManager
//        val rvAdapter = IconGridAdapter(IconGridAdapter.GridIconClickListener {
//            findNavController().navigate(it)
//        }, viewModel.scope)
//        binding.rvIconGrid.adapter = rvAdapter
////        rvAdapter.submitList(iconDataset.getChildImmunizationCategories(resources))
//    }
//
//}