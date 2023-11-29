package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_list

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.piramalswasthya.cho.R

class OutreachActiviityListFragment : Fragment() {

    companion object {
        fun newInstance() = OutreachActiviityListFragment()
    }

    private lateinit var viewModel: OutreachActiviityListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_outreach_activiity_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachActiviityListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}