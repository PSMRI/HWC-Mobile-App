package org.piramalswasthya.cho.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.piramalswasthya.cho.utils.setBoxColor
import androidx.compose.ui.res.booleanResource
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.DiagnosisValue
import org.piramalswasthya.cho.utils.setBoxColor

class DiagnosisAdapter(
    private val mContext: Context,
    private val isVisitDetail: Boolean? = null,
    private val isFollowupVisit: Boolean? = null,
    private val itemList: MutableList<DiagnosisValue>,
    private val itemChangeListener: RecyclerViewItemChangeListenerD
) : RecyclerView.Adapter<DiagnosisAdapter.ViewHolder>(){
    private var booleanVal:Boolean = false
    private val viewHolders = mutableListOf<DiagnosisAdapter.ViewHolder>()

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val diagnosisInput: TextInputEditText = itemView.findViewById(R.id.inputDignosis)
        val diagnosisInpuTextt: TextInputLayout = itemView.findViewById(R.id.diagnosis)
        val resetButton: FloatingActionButton = itemView.findViewById(R.id.resetButton)
        val cancelButton: FloatingActionButton = itemView.findViewById(R.id.deleteButton)

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
         it?.toString()?.let{
             itemData.diagnosis =  it
        }
        booleanVal = itemData.diagnosis.isNotEmpty()
        if(!booleanVal){
            holder.diagnosisInpuTextt.error
        }
        else{
            holder.diagnosisInpuTextt.error=null
        }
        holder.updateResetButtonState()
        itemChangeListener.onItemChanged()
    }

    if (isVisitDetail == true && isFollowupVisit == false){
        holder.resetButton.isVisible = false
        holder.cancelButton.isVisible = false
        holder.diagnosisInput.isClickable = false
        holder.diagnosisInput.isFocusable = false

        holder.diagnosisInpuTextt.boxBackgroundColor =
            ContextCompat.getColor(mContext, R.color.disable_field_color)
        holder.diagnosisInpuTextt.defaultHintTextColor = ColorStateList.valueOf(
            ContextCompat.getColor(mContext, R.color.disable_field_hint_color)
        )
    }

    // Update the visibility of the "Cancel" button for all items
    holder.updateDeleteButtonVisibility()

    // Update the visibility of the "Reset" button for all items
    holder.updateResetButtonState()

    fun isFieldFilled():Boolean{
        return booleanVal
    }
}


override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.diagnosis_adapter_layout, parent, false)
    val viewHolder = ViewHolder(view)
    viewHolders.add(viewHolder)
    return viewHolder
}

override fun getItemCount(): Int = itemList.size


    fun setError():Int{
        this.itemList.forEachIndexed {i , it ->
            if(it.diagnosis.isNullOrEmpty())
                return i
        }
        return -1
    }
}

interface  RecyclerViewItemChangeListenerD {
    fun onItemChanged()

}