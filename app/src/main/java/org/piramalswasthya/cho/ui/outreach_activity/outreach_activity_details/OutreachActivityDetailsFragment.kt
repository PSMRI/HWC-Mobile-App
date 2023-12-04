package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R

@AndroidEntryPoint
class OutreachActivityDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = OutreachActivityDetailsFragment()
    }

    private lateinit var viewModel: OutreachActivityDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_outreach_activity_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachActivityDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}