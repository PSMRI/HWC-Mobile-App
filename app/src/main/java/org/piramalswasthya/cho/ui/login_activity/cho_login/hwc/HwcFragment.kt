package org.piramalswasthya.cho.ui.login_activity.cho_login.hwc

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R

class HwcFragment : Fragment() {

    companion object {
        fun newInstance() = HwcFragment()
    }

    private lateinit var viewModel: HwcViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hwc, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HwcViewModel::class.java)
        // TODO: Use the ViewModel
    }

}