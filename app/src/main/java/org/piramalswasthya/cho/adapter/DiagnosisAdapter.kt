package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.DiagnosisValue

class DiagnosisAdapter(
    private val itemList: MutableList<DiagnosisValue>,
    private val itemChangeListener: RecyclerViewItemChangeListenerD
) : RecyclerView.Adapter<DiagnosisAdapter.ViewHolder>(){

    private val viewHolders = mutableListOf<DiagnosisAdapter.ViewHolder>()

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val diagnosisInput: TextInputEditText = itemView.findViewById(R.id.inputDignosis)

        val resetButton: Button = itemView.findViewById(R.id.resetButton)
        val cancelButton: Button = itemView.findViewById(R.id.deleteButton)

    init {
        // Set up click listener for the "Cancel" button
        cancelButton.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (itemCount > 1) deleteItem(position)
                updateDeleteButtonVisibility()
            }
        }

        // Set up click listener for the "Reset" button
        resetButton.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                resetFields(position)
            }
        }
    }

    fun updateResetButtonState() {
        val isItemFilled =
                diagnosisInput.text!!.isNotEmpty()

        resetButton.isEnabled = isItemFilled
    }

    fun updateDeleteButtonVisibility() {
        for (viewHolder in viewHolders) {
            viewHolder.cancelButton.isEnabled = itemCount > 1
        }
    }

    private fun deleteItem(position: Int) {
        if (position in 0 until itemList.size) {
            itemList.removeAt(position)
            notifyItemRemoved(position)
            itemChangeListener.onItemChanged()
        }
    }

    private fun resetFields(position: Int) {
        if (position in 0 until itemList.size) {
            val itemData = itemList[position]
            itemData.diagnosis = ""
            notifyItemChanged(position)
            itemChangeListener.onItemChanged()
        }
    }
}

override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val itemData = itemList[position]
    holder.diagnosisInput.setText(itemData.diagnosis)
    holder.cancelButton.isEnabled = itemCount > 1
    holder.resetButton.isEnabled = false

    holder.diagnosisInput.addTextChangedListener{
        itemData.diagnosis = it.toString()
        holder.updateResetButtonState()
        itemChangeListener.onItemChanged()
    }

    // Update the visibility of the "Cancel" button for all items
    holder.updateDeleteButtonVisibility()

    // Update the visibility of the "Reset" button for all items
    holder.updateResetButtonState()
}


override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.diagnosis_adapter_layout, parent, false)
    val viewHolder = ViewHolder(view)
    viewHolders.add(viewHolder)
    return viewHolder
}

override fun getItemCount(): Int = itemList.size

}
interface  RecyclerViewItemChangeListenerD {
    fun onItemChanged()
}