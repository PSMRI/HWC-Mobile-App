package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.ChiefComplaintMaster

class VisitDetailAdapter(
    private val itemList: MutableList<RecyclerItemData>,
    private val unitDropDown: List<String>,
    private val chiefComplaints : List<ChiefComplaintMaster>,
    private val itemChangeListener: RecyclerViewItemChangeListener,
    private val chiefComplaintsForFilter : List<ChiefComplaintMaster>
) :
    RecyclerView.Adapter<VisitDetailAdapter.ViewHolder>() {

    private val viewHolders = mutableListOf<ViewHolder>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chiefComplaintOptions: AutoCompleteTextView = itemView.findViewById(R.id.chiefComplaintDropDowns)
        val durationInput: TextInputEditText = itemView.findViewById(R.id.inputDuration)
        val durationUnitDropdown: AutoCompleteTextView = itemView.findViewById(R.id.dropdownDurUnit)
        val descriptionInput: TextInputEditText = itemView.findViewById(R.id.descInputText)
        val resetButton : Button = itemView.findViewById(R.id.resetButton)
        val cancelButton : Button = itemView.findViewById(R.id.deleteButton)

        init {
            // Set up click listener for the "Cancel" button
           cancelButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if(itemCount > 1) deleteItem(position)
                    updateDeleteButtonVisibility()
                }
            }

            // Set up click listener for the "Reset" button
            resetButton.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    resetFields(position)
                }
            }
        }
        fun updateResetButtonState() {
            val isItemFilled = chiefComplaintOptions.text.isNotEmpty() ||
                   durationInput.text!!.isNotEmpty() ||
                    durationUnitDropdown.text.isNotEmpty() ||
                    descriptionInput.text!!.isNotEmpty()
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
                itemData.chiefComplaint = ""
                itemData.duration = ""
                itemData.durationUnit = ""
                itemData.description = ""
                notifyItemChanged(position)
                itemChangeListener.onItemChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemData = itemList[position]
        // Bind data and set listeners for user interactions
        holder.chiefComplaintOptions.setText(itemData.chiefComplaint)
        holder.durationInput.setText(itemData.duration)
        holder.durationUnitDropdown.setText(itemData.durationUnit)
        holder.descriptionInput.setText(itemData.description)
        holder.cancelButton.isEnabled = itemCount > 1
        holder.resetButton.isEnabled = false

        // Set up dropdown adapter and populate dropdown values
        val unitDropdownAdapter = ArrayAdapter(holder.itemView.context, R.layout.drop_down, unitDropDown)
        val chiefComplaintAdapter = ChiefComplaintAdapter(holder.itemView.context, R.layout.drop_down, chiefComplaints,holder.chiefComplaintOptions,chiefComplaintsForFilter)
        holder.chiefComplaintOptions.setAdapter(chiefComplaintAdapter)
        holder.durationUnitDropdown.setAdapter(unitDropdownAdapter)

        holder.chiefComplaintOptions.setOnItemClickListener { parent, _, position, _ ->
            var chiefComplaint = parent.getItemAtPosition(position) as ChiefComplaintMaster
            holder.chiefComplaintOptions.setText(chiefComplaint?.chiefComplaint,false)
        }

        // Set listeners to update data when user interacts
        holder.chiefComplaintOptions.addTextChangedListener {
            itemData.chiefComplaint = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }
        holder.durationInput.addTextChangedListener {
            itemData.duration = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }
        holder.durationUnitDropdown.addTextChangedListener {
            itemData.durationUnit = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }
        holder.descriptionInput.addTextChangedListener {
            itemData.description = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        // Update the visibility of the "Cancel" button for all items
        holder.updateDeleteButtonVisibility()

        // Update the visibility of the "Reset" button for all items
        holder.updateResetButtonState()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.extra_chief_complaint_layout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolders.add(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int = itemList.size

}


data class RecyclerItemData(
    var chiefComplaint: String = "",
    var duration: String = "",
    var durationUnit: String = "",
    var description: String = ""
)

interface RecyclerViewItemChangeListener {
    fun onItemChanged()
}

