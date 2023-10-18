package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.ChiefComplaintValues

class VisitDetailAdapter(
    private val itemList: MutableList<ChiefComplaintValues>,
    private val unitDropDown: List<String>,
    private val chiefComplaints: List<ChiefComplaintMaster>,
    private val itemChangeListener: RecyclerViewItemChangeListener,
    private val chiefComplaintsForFilter: List<ChiefComplaintMaster>,
    private val endIconClickListener: EndIconClickListener
) :
    RecyclerView.Adapter<VisitDetailAdapter.ViewHolder>() {

    private val viewHolders = mutableListOf<ViewHolder>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chiefComplaintOptions: AutoCompleteTextView =
            itemView.findViewById(R.id.chiefComplaintDropDowns)
        val durationInput: TextInputEditText = itemView.findViewById(R.id.inputDuration)
        val durationUnitDropdown: AutoCompleteTextView = itemView.findViewById(R.id.dropdownDurUnit)
        val descriptionInput: TextInputEditText = itemView.findViewById(R.id.descInputText)
        val resetButton: FloatingActionButton = itemView.findViewById(R.id.resetButton)
        val cancelButton: FloatingActionButton = itemView.findViewById(R.id.deleteButton)
        val durationInputLayout: TextInputLayout = itemView.findViewById(R.id.duration)
        val descInputLayout: TextInputLayout = itemView.findViewById(R.id.descriptionText)
        val chiefComplaintOptionInput : TextInputLayout = itemView.findViewById(R.id.chiefComplaintOptions)
        val addButton : FloatingActionButton = itemView.findViewById(R.id.addButtonCC)
        val subtractButton : FloatingActionButton = itemView.findViewById(R.id.subtractButtonCC)
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
                itemData.durationUnit = "Days"
                itemData.description = ""
                notifyItemChanged(position)
                itemChangeListener.onItemChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var durationCount = 0
        val itemData = itemList[position]
        // Bind data and set listeners for user interactions
        holder.chiefComplaintOptions.setText(itemData.chiefComplaint)
        holder.durationInput.setText(itemData.duration)
        holder.durationUnitDropdown.setText(unitDropDown[1])
        holder.descriptionInput.setText(itemData.description)
        holder.cancelButton.isEnabled = itemCount > 1
        holder.resetButton.isEnabled = false

        // Set up dropdown adapter and populate dropdown values
        val unitDropdownAdapter =
            ArrayAdapter(holder.itemView.context, R.layout.drop_down, unitDropDown)
        val chiefComplaintAdapter = ChiefComplaintAdapter(
            holder.itemView.context,
            R.layout.drop_down,
            chiefComplaints,
            holder.chiefComplaintOptions,
            chiefComplaintsForFilter
        )
        holder.chiefComplaintOptions.setAdapter(chiefComplaintAdapter)
        holder.durationUnitDropdown.setAdapter(unitDropdownAdapter)

        holder.subtractButton.isEnabled = false
        holder.addButton.setOnClickListener {
                durationCount++
                holder.durationInput.setText(durationCount.toString())
                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()

            // Enable the "Subtract" button
            holder.subtractButton.isEnabled = true
        }
        holder.subtractButton.setOnClickListener {
            if (durationCount > 1) {
                durationCount--
                holder.durationInput.setText(durationCount.toString())
                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()
            }
        }


        holder.chiefComplaintOptions.setOnItemClickListener { parent, _, position, _ ->
            var chiefComplaint = parent.getItemAtPosition(position) as ChiefComplaintMaster
            holder.chiefComplaintOptions.setText(chiefComplaint?.chiefComplaint, false)
            itemData.id = chiefComplaint.chiefComplaintID
        }

        // Set listeners to update data when user interacts
        holder.chiefComplaintOptions.addTextChangedListener {
                if(chiefComplaints.map{it.chiefComplaint}.contains(it.toString())){
                    itemData.chiefComplaint = it.toString()
                    holder.updateResetButtonState()
                    itemChangeListener.onItemChanged()
                    holder.chiefComplaintOptionInput.apply {
                        boxStrokeColor = resources.getColor(R.color.purple)
                        hintTextColor = defaultHintTextColor
                    }
                }else{
                    itemData.chiefComplaint = ""
                    holder.chiefComplaintOptionInput.apply {
                        requestFocus()
                        boxStrokeColor = Color.RED
                        hintTextColor = ColorStateList.valueOf(Color.RED)
                    }
                }
        }
        holder.durationInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank() && s.length == 1 && s[0] == '0') s.clear()
                itemData.duration = s.toString()

                if (s.isNullOrEmpty()) {
                    holder.durationInputLayout.apply {
                        requestFocus()
                        boxStrokeColor = Color.RED
                        hintTextColor = ColorStateList.valueOf(Color.RED)
                    }
                } else {
                    holder.durationInputLayout.apply {
                        boxStrokeColor = resources.getColor(R.color.purple)
                        hintTextColor = defaultHintTextColor
                    }
                }

                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()
            }
        })
        holder.durationUnitDropdown.addTextChangedListener {
            itemData.durationUnit = it.toString()
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

        // Voice to text converter click listeners

        holder.descInputLayout.setEndIconOnClickListener {
            endIconClickListener.onEndIconDescClick(position)
        }
//        holder.chiefComplaintOptionInput.setEndIconOnClickListener {
//            endIconClickListener.onEndIconChiefClick(position)
//        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.extra_chief_complaint_layout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolders.add(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int = itemList.size

}

interface RecyclerViewItemChangeListener {
    fun onItemChanged()
}

interface EndIconClickListener {
    fun onEndIconDurationClick(position: Int)
    fun onEndIconDescClick(position: Int)
    fun onEndIconChiefClick(position: Int)
}
