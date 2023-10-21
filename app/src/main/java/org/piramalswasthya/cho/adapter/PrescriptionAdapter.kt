package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.PrescriptionValues
import org.piramalswasthya.cho.model.PrescriptionValuesForTemplate
import org.piramalswasthya.cho.ui.commons.case_record.FormItemAdapter
import org.piramalswasthya.cho.ui.setSpinnerItems

class PrescriptionAdapter(
    private val listTemplate: MutableList<PrescriptionValuesForTemplate>,
    private val itemList: MutableList<PrescriptionValuesForTemplate>,
    private val formMD: List<ItemMasterList>,
    private val frequencyDropDown: List<String>,
    private val unitDropDown: List<String>,
    private val instructionDropdown: List<String>,
    private val itemMasterForFilter: List<ItemMasterList>,
    private val itemChangeListener: RecyclerViewItemChangeListenersP
) :
    RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

    private var durationCount = 0
    private val maxDuration = 6

    private val viewHolders = mutableListOf<PrescriptionAdapter.ViewHolder>()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formOptions: AutoCompleteTextView =
            itemView.findViewById(R.id.dosagesDropDownVal)
        val frequencyOptions: AutoCompleteTextView =
            itemView.findViewById(R.id.frequencyDropDownVal)
        val durationInput: TextInputEditText = itemView.findViewById(R.id.inputDuration)
        val instructionOption: AutoCompleteTextView = itemView.findViewById(R.id.inputInstruction)
        val unitOption: AutoCompleteTextView =
            itemView.findViewById(R.id.unitDropDownVal)
        val tempText : TextInputLayout = itemView.findViewById(R.id.tempName)
        val tempName : TextInputEditText = itemView.findViewById(R.id.inputTestName)
        val resetButton: FloatingActionButton = itemView.findViewById(R.id.resetButton)
        val cancelButton: FloatingActionButton = itemView.findViewById(R.id.deleteButton)
        val addButton : FloatingActionButton = itemView.findViewById(R.id.addButton)
        val subtractButton : FloatingActionButton = itemView.findViewById(R.id.subtractButton)
        val saveTemplate : Button = itemView.findViewById(R.id.saveTemplate)

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
                    frequencyOptions.text.isNotEmpty() ||
                    durationInput.text!!.isNotEmpty() ||
                    instructionOption.text!!.isNotEmpty() ||
                    tempName.text!!.isNotEmpty() ||
                    unitOption.text.isNotEmpty()
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
                itemData.frequency = ""
                itemData.duration = ""
                itemData.instruction = ""
                itemData.unit = ""
                itemData.tempName =""
                notifyItemChanged(position)
                itemChangeListener.onItemChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        durationCount=0
        val itemData = itemList[position]
       holder.subtractButton.isEnabled = false
        holder.addButton.setOnClickListener {
            if (durationCount < maxDuration) {
                durationCount++
                holder.durationInput.setText(durationCount.toString())
                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()
            }
            // Disable the "Add" button when the duration count reaches the maximum
            if (durationCount >= maxDuration) {
                holder.addButton.isEnabled = false
            }
            // Enable the "Subtract" button
            holder.subtractButton.isEnabled = true
        }

        // Set up click listener for the "Subtract" button
        holder.subtractButton.setOnClickListener {
            if (durationCount > 1) {
                durationCount--
                holder.durationInput.setText(durationCount.toString())
                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()
            } else if (durationCount == 1) {
                // When durationCount is 1, show the hint "duration" and disable "Subtract"
                holder.durationInput.hint = holder.itemView.context.getString(R.string.duration2)
                holder.durationInput.text = null
                holder.subtractButton.isEnabled = false
            }
            // Enable the "Add" button
            holder.addButton.isEnabled = true
        }

        holder.saveTemplate.setOnClickListener {
                holder.tempText.visibility = View.VISIBLE
                val testName = holder.tempName.text.toString()
            if (testName.isNotEmpty()) {
                if (isTestNameUnique(testName)) {
                    if (position < itemList.size) {
                        val prescriptionToSave = itemList[position]
                        listTemplate.add(prescriptionToSave.copy())
                        showSavedToast(holder.itemView.context)
                    }
                } else {
                    showTestNameNotUniqueError(holder.itemView.context)
                }
            }else {
                showSavedToastErro(holder.itemView.context)
                holder.tempName.requestFocus()
            }
        }

        holder.formOptions.setText(itemData.form)
        holder.frequencyOptions.setText(itemData.frequency)
        holder.durationInput.setText(itemData.duration)
        holder.instructionOption.setText(itemData.instruction)
        holder.tempName.setText(itemData.tempName)
        holder.unitOption.setText(unitDropDown[0])
        holder.cancelButton.isEnabled = itemCount > 1
        holder.resetButton.isEnabled = false

        val formItemAdapter = FormItemAdapter(
            holder.itemView.context,
            R.layout.drop_down,
            formMD,
            holder.formOptions,
            itemMasterForFilter
        )
        holder.formOptions.setAdapter(formItemAdapter)

        holder.formOptions.setOnItemClickListener { parent, _, position, abc ->
            val selectedString = parent.getItemAtPosition(position)
            val form = formMD.first { it.dropdownForMed == selectedString }
            holder.formOptions.setText(form.dropdownForMed,false)
            itemData.id = form.itemID
        }

        val frequencyAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, frequencyDropDown)
        holder.frequencyOptions.setAdapter(frequencyAdapter)

        val unitAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, unitDropDown)
        holder.unitOption.setAdapter(unitAdapter)

        val insAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, instructionDropdown)
        holder.instructionOption.setAdapter(insAdapter)

        formMD.map { it.dropdownForMed }.toTypedArray()
            ?.let { holder.formOptions.setSpinnerItems(it) }

        holder.formOptions.addTextChangedListener{
            itemData.form= it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.tempName.addTextChangedListener{
            itemData.tempName= it.toString()
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

        holder.instructionOption.addTextChangedListener {
            itemData.instruction = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.unitOption.addTextChangedListener {
            itemData.unit = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }
        // Update the visibility of the "Cancel" button for all items
        holder.updateDeleteButtonVisibility()

        // Update the visibility of the "Reset" button for all items
        holder.updateResetButtonState()
    }
    private fun isTestNameUnique(testName: String): Boolean {
        for (item in listTemplate) {
            if (item.tempName == testName) {
                return false
            }
        }
        return true
    }

    private fun showTestNameNotUniqueError(context: Context) {
        Toast.makeText(context, "Test name must be unique", Toast.LENGTH_SHORT).show()
    }


    private fun showSavedToast(context: Context) {
        Toast.makeText(context, "Prescription Template Saved", Toast.LENGTH_SHORT).show()
    }
    private fun showSavedToastErro(context: Context) {
        Toast.makeText(context, "Enter the Template Name", Toast.LENGTH_SHORT).show()
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