package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_list

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.ActivityItemAdapter
import org.piramalswasthya.cho.adapter.AncVisitAdapter
import org.piramalswasthya.cho.databinding.FragmentOutreachActiviityListBinding
import org.piramalswasthya.cho.databinding.FragmentOutreachActivityFormBinding
import org.piramalswasthya.cho.ui.abha_id_activity.verify_mobile_otp.VerifyMobileOtpFragmentDirections
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.FragmentVisitDetailDirections
import org.piramalswasthya.cho.ui.outreach_activity.OutreachActivity

@AndroidEntryPoint
class OutreachActiviityListFragment : Fragment(), NavigationAdapter {

    companion object {
        fun newInstance() = OutreachActiviityListFragment()
    }

    private val binding by lazy{
        FragmentOutreachActiviityListBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: OutreachActiviityListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachActiviityListViewModel::class.java)

        binding.activityList.adapter =
            ActivityItemAdapter(ActivityItemAdapter.ActivityClickListener { activity ->
                findNavController().navigate(
                    OutreachActiviityListFragmentDirections.actionOutreachActiviityListFragmentToOutreachActivityDetailsFragment(
//                        activity
                    )
                )
            })

        viewModel.isDataLoaded.observe(viewLifecycleOwner){
            if(it == true){
                (binding.activityList.adapter as ActivityItemAdapter).submitList(viewModel.activityList)
            }
        }
        // TODO: Use the ViewModel
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_outreach_activity_list
    }

    override fun onSubmitAction() {
        findNavController().navigate(
            OutreachActiviityListFragmentDirections.actionOutreachActiviityListFragmentToOutreachActivityFormFragment()
        )
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }

}