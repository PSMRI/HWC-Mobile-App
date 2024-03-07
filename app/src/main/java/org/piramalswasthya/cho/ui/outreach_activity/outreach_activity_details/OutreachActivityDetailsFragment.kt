package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_details

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
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
import org.piramalswasthya.cho.utils.ImgUtils
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
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityDetails = args.activityDetails
        viewModel = ViewModelProvider(this).get(OutreachActivityDetailsViewModel::class.java)

        viewModel.loadData(activityDetails.activityId!!)

        viewModel.isDataLoaded.observe(viewLifecycleOwner){
            if(viewModel.isDataLoaded.value == true){
                if(viewModel.outreachActivityNetworkModel?.img1 != null){
                    val bitmap = ImgUtils.decodeBase64ToBitmap(viewModel.outreachActivityNetworkModel?.img1!!)
                    if(bitmap != null){
                        binding.iv1.visibility = View.VISIBLE
                        binding.iv1.setImageBitmap(bitmap)
                    }
                    Log.d("image 1 is", viewModel.outreachActivityNetworkModel?.img1!!)
                }
                if(viewModel.outreachActivityNetworkModel?.img2 != null){
                    val bitmap = ImgUtils.decodeBase64ToBitmap(viewModel.outreachActivityNetworkModel?.img2!!)
                    if(bitmap != null){
                        binding.iv2.visibility = View.VISIBLE
                        binding.iv2.setImageBitmap(bitmap)
                    }
                    Log.d("image 2 is", viewModel.outreachActivityNetworkModel?.img2!!)
                }
            }
        }

        binding.activityName.text = activityDetails.activityName
        binding.createDate.text = DateTimeUtil.formatActivityDate(activityDetails.activityDate)
        binding.eventDesc.text = activityDetails.eventDescription
        binding.paricipants.text = activityDetails.noOfParticipants?.toString()

    }

}