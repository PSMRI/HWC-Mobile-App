package org.piramalswasthya.cho.ui.commons.history_custom.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.piramalswasthya.cho.R

class IllnessDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.illness_dialog_box, null)

        val illnessTextView = customView.findViewById<TextView>(R.id.illness)
        val durationTextView = customView.findViewById<TextView>(R.id.duration)
        val timePeriodTextView = customView.findViewById<TextView>(R.id.time_period_ago)

        illnessTextView.text = "Fever"
        durationTextView.text = "3 days"
        timePeriodTextView.text = "1 month ago"

        val closeButton = customView.findViewById<Button>(R.id.btnClose)


        val alertDialogBuilder = AlertDialog.Builder(requireContext())
            .setView(customView)

        closeButton.setOnClickListener {
            dismiss()
        }

        return alertDialogBuilder.create()
    }
}
