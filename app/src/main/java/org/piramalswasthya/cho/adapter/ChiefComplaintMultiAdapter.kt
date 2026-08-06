package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.utils.MasterDataLocalizer

class ChiefComplaintMultiAdapter(
    private val chiefComplaints: List<ChiefComplaintDB>,
    val layoutVariant: String? = null
) :
    RecyclerView.Adapter<ChiefComplaintMultiAdapter.ViewHolder>() {

    // ViewHolder class for caching views
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chiefComplaintName: TextInputEditText =
            itemView.findViewById(R.id.chiefComplaintDropDowns)
        val durationInput: TextInputEditText = itemView.findViewById(R.id.inputDuration)
        val durationUnitInput: TextInputEditText = itemView.findViewById(R.id.inputDurationUnit)

        val tvChiefComplaint: TextView = itemView.findViewById(R.id.tvChiefComplaint)
        val tvDurationValue: TextView = itemView.findViewById(R.id.tvDurationValue)
        val oldLayout: ConstraintLayout = itemView.findViewById(R.id.oldLayout)
        val newLayout: ConstraintLayout = itemView.findViewById(R.id.newLayout)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.chief_complaint_multi_adapter, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chiefComplaint = chiefComplaints[position]
        val ctx = holder.itemView.context

        holder.oldLayout.visibility = if (layoutVariant == null) View.VISIBLE else View.GONE
        holder.newLayout.visibility = if (layoutVariant == null) View.GONE else View.VISIBLE

        val localizedChiefComplaint =
            MasterDataLocalizer.localizeChiefComplaint(ctx, chiefComplaint.chiefComplaint)
        val localizedDurationUnit =
            MasterDataLocalizer.localizeDurationUnit(ctx, chiefComplaint.durationUnit)

        holder.chiefComplaintName.setText(localizedChiefComplaint)
        holder.durationInput.setText(chiefComplaint.duration)
        holder.durationUnitInput.setText(localizedDurationUnit)

        holder.tvChiefComplaint.text = localizedChiefComplaint
        holder.tvDurationValue.text = "${chiefComplaint.duration} $localizedDurationUnit"
        holder.tvDescription.text = chiefComplaint.description

        // Add a TextWatcher to update the duration property when the EditText changes
//        holder.durationInput.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                val durationText = s.toString()
//                chiefComplaint.duration = if (durationText.isNotEmpty()) durationText.toInt() else 0
//            }
//        })
    }

    override fun getItemCount(): Int {
        return chiefComplaints.size
    }
}