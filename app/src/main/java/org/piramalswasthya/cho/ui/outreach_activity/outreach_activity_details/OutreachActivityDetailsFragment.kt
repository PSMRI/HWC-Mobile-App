package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_details

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOutreachActivityDetailsBinding
import org.piramalswasthya.cho.databinding.FragmentOutreachActivityFormBinding
import org.piramalswasthya.cho.model.OutreachActivityNetworkModel
import org.piramalswasthya.cho.ui.commons.lab_technician.LabTechnicianFormFragmentArgs
import org.piramalswasthya.cho.utils.DateTimeUtil
import javax.inject.Inject

@AndroidEntryPoint
class OutreachActivityDetailsFragment : Fragment() {

    private lateinit var viewModel: OutreachActivityDetailsViewModel

    private val binding by lazy{
        FragmentOutreachActivityDetailsBinding.inflate(layoutInflater)
    }

    private val args: OutreachActivityDetailsFragmentArgs by lazy {
        OutreachActivityDetailsFragmentArgs.fromBundle(requireArguments())
    }

    private lateinit var activityDetails : OutreachActivityNetworkModel;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachActivityDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityDetails = args.activityDetails

        binding.activityName.text = activityDetails.activityName
        binding.createDate.text = DateTimeUtil.formatActivityDate(activityDetails.activityDate)
        binding.eventDesc.text = activityDetails.eventDescription
        binding.paricipants.text = activityDetails.noOfParticipants?.toString()

//        viewModel.activity.activityDate = DateTimeUtil.formatActivityDate(viewModel.activity.activityDate)
//        binding.outreachActivityModel = viewModel.activity
//        binding.outreachActivityModel.activityDate = DateTimeUtil.formatActivityDate(binding.outreachActivityModel.activityDate)
//        binding.outreachActivityDetailsViewModel =
    }

}