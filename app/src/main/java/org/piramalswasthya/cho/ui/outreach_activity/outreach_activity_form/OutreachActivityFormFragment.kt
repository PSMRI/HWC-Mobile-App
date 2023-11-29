package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_form

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.piramalswasthya.cho.R

class OutreachActivityFormFragment : Fragment() {

    companion object {
        fun newInstance() = OutreachActivityFormFragment()
    }

    private lateinit var viewModel: OutreachActivityFormViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_outreach_activity_form, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachActivityFormViewModel::class.java)
        // TODO: Use the ViewModel
    }

}