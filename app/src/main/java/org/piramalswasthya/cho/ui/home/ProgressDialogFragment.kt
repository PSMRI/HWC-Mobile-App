package org.piramalswasthya.cho.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentProgressDialogBinding


class ProgressDialogFragment : DialogFragment() {

    private var _binding: FragmentProgressDialogBinding?= null
    private val binding: FragmentProgressDialogBinding
        get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProgressDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updateProgress(filesSent: Int, totalFiles: Int, progress: Int) {
       binding.apply{
            tvRecordsTransferred.text = "Records Transferred: $filesSent/$totalFiles"
            progressBar.progress = progress
            tvProgressPercentage.text = "$progress%"
        }
    }

    fun updateUI(status: String){
        if(status == "Success"){
            binding.statusTv.text = "Files transferred successfully"
        }else if(status == "Error"){
            binding.statusTv.text = "Error in transferring files"
        }
    }

}