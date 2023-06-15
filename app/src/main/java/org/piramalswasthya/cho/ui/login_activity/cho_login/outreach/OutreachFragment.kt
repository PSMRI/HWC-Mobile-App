package org.piramalswasthya.cho.ui.login_activity.cho_login.outreach

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.piramalswasthya.cho.R

class OutreachFragment : Fragment() {

    companion object {
        fun newInstance() = OutreachFragment()
    }

    private lateinit var viewModel: OutreachViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_outreach, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachViewModel::class.java)
        // TODO: Use the ViewModel
    }

}