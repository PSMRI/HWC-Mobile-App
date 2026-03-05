package org.piramalswasthya.cho.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.PrescriptionValues
import org.piramalswasthya.cho.ui.commons.case_record.FormItemAdapter
import org.piramalswasthya.cho.ui.setSpinnerItems
import org.piramalswasthya.cho.utils.HelperUtil

class PrescriptionAdapter(
    private val isVisitDetail: Boolean? = null,
    private val isFollowupVisit: Boolean? = null,
    private val isMedicineDispensedByPharmacist: Boolean = false,
    private var dispensedLockedItemCount: Int = 0,
    private val itemList: MutableList<PrescriptionValues>,
    private val formMD: List<ItemMasterList>,
    private val frequencyDropDown: List<String>,
    private val unitDropDown: List<String>,
    private val instructionDropdown: List<String>,
    private val itemMasterForFilter: List<ItemMasterList>,
    private val itemChangeListener: RecyclerViewItemChangeListenersP,
    private val fetchStockListener: ((Int, (Int) -> Unit) -> Unit)? = null
) : RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

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
        val resetButton: FloatingActionButton = itemView.findViewById(R.id.resetButton)
        val cancelButton: FloatingActionButton = itemView.findViewById(R.id.deleteButton)
        val addButton : TextView = itemView.findViewById(R.id.addButton)
        val subtractButton : TextView = itemView.findViewById(R.id.subtractButton)
        val textPrescriptionHeading : TextView = itemView.findViewById(R.id.textPrescriptionHeading)
        val textStock : TextView = itemView.findViewById(R.id.textStock)

//        Dropdown Fields
        val formOptionsDropDown: TextInputLayout = itemView.findViewById(R.id.dosagesDropDown)
        val frequencyOptionsDropDown: TextInputLayout = itemView.findViewById(R.id.frequencyDropDown)
        val unitOptionDropDown: TextInputLayout = itemView.findViewById(R.id.unitDropDown)
        val instructionOptionDropDown: TextInputLayout = itemView.findViewById(R.id.instruction)

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
                    frequencyOptions.text.isNotEmpty() ||
                    durationInput.text!!.isNotEmpty() ||
                    instructionOption.text!!.isNotEmpty() ||
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
                notifyItemRangeChanged(position, itemList.size - position)

                itemChangeListener.onItemChanged()
            }
        }

        private fun resetFields(position: Int) {
            if (position in 0 until itemList.size) {
                val itemData = itemList[position]
                itemData.form = ""
                itemData.frequency = ""
                itemData.duration = ""
                itemData.instructions = ""
                itemData.unit = ""
                itemData.id = null
                textStock.text = ""
                notifyItemChanged(position)
                itemChangeListener.onItemChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        durationCount=0
        val itemData = itemList[position]
        
        android.util.Log.d("PrescriptionAdapter", "onBindViewHolder: position=$position, title=${itemData.title}, isDispensed=${itemData.isDispensed}, form=${itemData.form}, id=${itemData.id}")
        
       holder.subtractButton.isEnabled = false
        holder.addButton.setOnClickListener {
            durationCount = itemData.duration.toIntOrNull()?.takeIf { it <= maxDuration } ?: 0
            if (durationCount < maxDuration) {
                durationCount++
                holder.durationInput.setText(durationCount.toString())
                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()
            }
            // Disable the "Add" button when the duration count reaches the maximum
            if (durationCount >= maxDuration) {
                Toast.makeText(holder.itemView.context, holder.itemView.context.getString(R.string.max_duration_allowed_6), Toast.LENGTH_SHORT).show()
                holder.addButton.isEnabled = false
            }
            // Enable the "Subtract" button
            holder.subtractButton.isEnabled = true
        }

        // Set up click listener for the "Subtract" button
        holder.subtractButton.setOnClickListener {
            durationCount = if(itemData.duration.isNullOrEmpty()){
                0
            }else {
                itemData.duration.toInt()
            }
            if (durationCount > 1) {
                durationCount--
                holder.durationInput.setText(durationCount.toString())
                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()
            } else if (durationCount == 1) {
                // When durationCount is 1, show the hint "duration" and disable "Subtract"
                holder.durationInput.hint = holder.itemView.context.getString(R.string.duration_prescription)
                holder.durationInput.text = null
                holder.subtractButton.isEnabled = false
            }
            // Enable the "Add" button
            holder.addButton.isEnabled = true
        }

        // Read-only in view mode; for dispensed medicine only initial rows are locked, newly added rows stay editable.
        val isCaseReadOnly = (isVisitDetail == true && isFollowupVisit == false)
        val isDispensedLockedRow = isMedicineDispensedByPharmacist && position < dispensedLockedItemCount
        val isRowReadOnly = isCaseReadOnly || isDispensedLockedRow
        val hasData = itemData.form.isNotBlank() || itemData.frequency.isNotBlank() ||
                itemData.duration.isNotBlank() || itemData.instructions.isNotBlank() || itemData.unit.isNotBlank()
        holder.itemView.visibility = if (isRowReadOnly && !hasData) View.GONE else View.VISIBLE

        if (isRowReadOnly) {
            holder.subtractButton.isEnabled = false
            holder.addButton.isEnabled = false

            HelperUtil.disableDropdownField(holder.formOptions, holder.formOptionsDropDown)
            HelperUtil.disableDropdownField(holder.frequencyOptions, holder.frequencyOptionsDropDown)
            HelperUtil.disableDropdownField(holder.unitOption, holder.unitOptionDropDown)
            HelperUtil.disableDropdownField(holder.instructionOption, holder.instructionOptionDropDown)

            holder.durationInput.isFocusable = false
            holder.durationInput.isClickable = false

            holder.resetButton.isVisible = false
            holder.cancelButton.isVisible = false
        } else {
            holder.resetButton.isVisible = true
            holder.cancelButton.isVisible = true
        }

        // Bind data WITHOUT triggering text watchers (set data before adding listeners)
        // Use setText with BufferType.EDITABLE to avoid triggering watchers
        holder.formOptions.setText(itemData.form, false)
        holder.frequencyOptions.setText(itemData.frequency, false)
        holder.durationInput.setText(itemData.duration)
        holder.instructionOption.setText(itemData.instructions, false)
        holder.unitOption.setText(itemData.unit, false)
        holder.cancelButton.isEnabled = itemCount > 1
        holder.resetButton.isEnabled = false
        
        android.util.Log.d("PrescriptionAdapter", "Data bound: position=$position, form='${itemData.form}', frequency='${itemData.frequency}', duration='${itemData.duration}'")
        
        // Show "Dispensed" label in green for dispensed medicines
        if (itemData.isDispensed) {
            holder.textPrescriptionHeading.text = "${itemData.title} - Dispensed"
            holder.textPrescriptionHeading.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.stock_green))
        } else {
            holder.textPrescriptionHeading.text = itemData.title
            holder.textPrescriptionHeading.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }

        if(itemData.id!=null){
            var st = formMD.find { it.itemID==itemData.id }
            holder.formOptions.setText(st?.dropdownForMed.toString(), false)
            
            android.util.Log.d("PrescriptionAdapter", "Setting form from ID: position=$position, id=${itemData.id}, form=${st?.dropdownForMed}")
            
            itemData.id?.let { id ->
                fetchStockListener?.invoke(id) { stock ->
                    if (stock > 0) {
                        holder.textStock.text = holder.itemView.context.getString(R.string.stock_label, stock)
                        holder.textStock.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.stock_green))
                    } else {
                        holder.textStock.text = holder.itemView.context.getString(R.string.out_of_stock)
                        holder.textStock.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.stock_red))
                    }
                }
            }
        } else {
            android.util.Log.d("PrescriptionAdapter", "No ID for position=$position, clearing stock text")
            holder.textStock.text = ""
        }

//       holder.formOptions.setSpinnerItems(formMD.map { it.dropdownForMed }.toTypedArray())

        val formItemAdapter = FormItemAdapter(
            holder.itemView.context,
            R.layout.drop_down,
            formMD,
            holder.formOptions,
            itemMasterForFilter
        )
        holder.formOptions.setAdapter(formItemAdapter)

        holder.formOptions.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()

            val selectedItem = formMD.find { it.dropdownForMed.equals(selectedName) }

            selectedItem?.let {
                holder.formOptions.setText(selectedName)
                itemData.id = it.itemID
                
                it.itemID?.let { id ->
                    fetchStockListener?.invoke(id) { stock ->
                        if (stock > 0) {
                            holder.textStock.text = holder.itemView.context.getString(R.string.stock_label, stock)
                            holder.textStock.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.stock_green))
                        } else {
                            holder.textStock.text = holder.itemView.context.getString(R.string.out_of_stock)
                            holder.textStock.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.stock_red))
                        }
                    }
                }
            }
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


        holder.frequencyOptions.addTextChangedListener {
            itemData.frequency = it.toString()
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }

        holder.durationInput.addTextChangedListener (object : TextWatcher {override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank() && s.length == 1 && s[0] == '0') s.clear()
                if (!s.isNullOrBlank() && s.toString().toInt()>6) {
                    s.clear()
                    Toast.makeText(holder.itemView.context, holder.itemView.context.getString(R.string.max_duration_allowed_6), Toast.LENGTH_SHORT).show()
                }
                else {
                    itemData.duration = s.toString()
                    holder.updateResetButtonState()
                    itemChangeListener.onItemChanged()
                }
            }
        })

        holder.instructionOption.addTextChangedListener {
            itemData.instructions = it.toString()
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

        // Don't overwrite the title if it's already set (especially for dispensed medicines)
        // The title with "Dispensed" label is already set above
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.prescription_custome_layout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolders.add(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int = itemList.size

    fun setDispensedLockedItemCount(count: Int) {
        dispensedLockedItemCount = count.coerceAtLeast(0)
        notifyDataSetChanged()
    }

}
interface RecyclerViewItemChangeListenersP {
    fun onItemChanged()
}