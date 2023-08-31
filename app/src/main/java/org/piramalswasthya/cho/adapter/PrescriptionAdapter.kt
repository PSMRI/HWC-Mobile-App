package org.piramalswasthya.cho.adapter

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
import org.piramalswasthya.cho.model.PrescriptionValues
import org.piramalswasthya.cho.ui.setSpinnerItems

class PrescriptionAdapter(
    private val itemList: MutableList<PrescriptionValues>,
    private val formDropDown: List<String>,
    private val dosageDropDown: List<String>,
    private val frequencyDropDown: List<String>,
    private val unitDropDown: List<String>,
    private val routeDropdown: List<String>,
    private val itemChangeListener: RecyclerViewItemChangeListenersP
) :
    RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

    private val viewHolders = mutableListOf<PrescriptionAdapter.ViewHolder>()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formOptions: AutoCompleteTextView =
            itemView.findViewById(R.id.formDropDownVal)
        val medicineOption: TextInputEditText = itemView.findViewById(R.id.inputMedicine)
        val dosageOptions: AutoCompleteTextView =
            itemView.findViewById(R.id.dosagesDropDownVal)
        val frequencyOptions: AutoCompleteTextView =
            itemView.findViewById(R.id.frequencyDropDownVal)
        val durationInput: TextInputEditText = itemView.findViewById(R.id.inputDuration)
        val instructionInput: TextInputEditText = itemView.findViewById(R.id.inputInstruction)
        val unitOption: AutoCompleteTextView =
            itemView.findViewById(R.id.unitDropDownVal)
        val routeOption: AutoCompleteTextView =
            itemView.findViewById(R.id.routeDropDownVal)
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
            val isItemFilled = formOptions.text.isNotEmpty() ||
                    formOptions.text.isNotEmpty() ||
                    medicineOption.text!!.isNotEmpty() ||
                    dosageOptions.text.isNotEmpty() ||
                    frequencyOptions.text.isNotEmpty() ||
                    durationInput.text!!.isNotEmpty() ||
                    instructionInput.text!!.isNotEmpty() ||
                    unitOption.text.isNotEmpty() ||
                    routeOption.text.isNotEmpty()
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
                itemData.form = ""
                itemData.medicine = ""
                itemData.dosage = ""
                itemData.frequency = ""
                itemData.duration = ""
                itemData.instruction = ""
                itemData.unit = ""
                itemData.route = ""
                notifyItemChanged(position)
                itemChangeListener.onItemChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemData = itemList[position]
        // Bind data and set listeners for user interactions
        holder.formOptions.setText(itemData.form)
        holder.medicineOption.setText(itemData.medicine)
        holder.dosageOptions.setText(itemData.dosage)
        holder.frequencyOptions.setText(itemData.frequency)
        holder.durationInput.setText(itemData.duration)
        holder.instructionInput.setText(itemData.instruction)
        holder.unitOption.setText(itemData.unit)
        holder.routeOption.setText(itemData.route)
        holder.cancelButton.isEnabled = itemCount > 1
        holder.resetButton.isEnabled = false

//        val illnessDropdownAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_dropdown_item, illnessDropdown.map { it.illnessType })

//        holder.illnessOptions.setAdapter(illnessDropdownAdapter)


        // Set up dropdown adapter and populate dropdown values
        val formAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, formDropDown)
        holder.formOptions.setAdapter(formAdapter)

        val dosageAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, dosageDropDown)
        holder.dosageOptions.setAdapter(dosageAdapter)

        val frequencyAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, frequencyDropDown)
        holder.frequencyOptions.setAdapter(frequencyAdapter)

        val unitAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, unitDropDown)
        holder.unitOption.setAdapter(unitAdapter)

        val routeAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, routeDropdown)
        holder.routeOption.setAdapter(routeAdapter)

        holder.formOptions.addTextChangedListener {
            itemData.form = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.medicineOption.addTextChangedListener {
            itemData.medicine = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.dosageOptions.addTextChangedListener {
            itemData.dosage = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.frequencyOptions.addTextChangedListener {
            itemData.frequency = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.durationInput.addTextChangedListener {
            itemData.duration = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.instructionInput.addTextChangedListener {
            itemData.instruction = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.unitOption.addTextChangedListener {
            itemData.unit = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }
        holder.routeOption.addTextChangedListener {
            itemData.route = it.toString()
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
            .inflate(R.layout.prescription_custome_layout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolders.add(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int = itemList.size

}
interface RecyclerViewItemChangeListenersP {
    fun onItemChanged()
}