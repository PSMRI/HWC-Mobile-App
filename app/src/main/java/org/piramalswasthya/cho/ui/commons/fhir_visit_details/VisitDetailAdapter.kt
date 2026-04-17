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
import android.widget.TextView
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chiefComplaintOptions: AutoCompleteTextView =
            itemView.findViewById(R.id.chiefComplaintDropDowns)
        val durationInput: TextInputEditText = itemView.findViewById(R.id.inputDuration)
        val durationUnitDropdown: AutoCompleteTextView = itemView.findViewById(R.id.dropdownDurUnit)
        val descriptionInput: TextInputEditText = itemView.findViewById(R.id.descInputText)
        val resetButton: FloatingActionButton = itemView.findViewById(R.id.resetButton)
        val cancelButton: FloatingActionButton = itemView.findViewById(R.id.deleteButton)
      //  val durationInputLayout: TextInputLayout = itemView.findViewById(R.id.duration)
        val descInputLayout: TextInputLayout = itemView.findViewById(R.id.descriptionText)
        val chiefComplaintOptionInput : TextInputLayout = itemView.findViewById(R.id.chiefComplaintOptions)
//        val addButton : FloatingActionButton = itemView.findViewById(R.id.addButtonCC)
//        val subtractButton : FloatingActionButton = itemView.findViewById(R.id.subtractButtonCC)

        val addButton : TextView = itemView.findViewById(R.id.addButton)
        val subtractButton : TextView = itemView.findViewById(R.id.subtractButton)

        var isBinding = false

        init {
            cancelButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (itemCount > 1) deleteItem(position)
                }
            }

            resetButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    resetFields(position)
                }
            }

            addButton.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                val itemData = itemList[position]
                val durationCount = itemData.duration?.toIntOrNull() ?: 0
                if (durationCount < 99) {
                    durationInput.setText((durationCount + 1).toString())
                    subtractButton.isEnabled = true
                }
            }

            subtractButton.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                val itemData = itemList[position]
                val durationCount = itemData.duration?.toIntOrNull() ?: 0
                if (durationCount > 1) {
                    durationInput.setText((durationCount - 1).toString())
                }
            }

            chiefComplaintOptions.setOnItemClickListener { parent, _, selectedPosition, _ ->
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnItemClickListener
                val chiefComplaint = parent.getItemAtPosition(selectedPosition) as ChiefComplaintMaster
                chiefComplaintOptions.setText(chiefComplaint.chiefComplaint, false)
                itemList[position].id = chiefComplaint.chiefComplaintID
            }

            chiefComplaintOptions.addTextChangedListener {
                if (isBinding) return@addTextChangedListener
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@addTextChangedListener
                val itemData = itemList[position]

                if (chiefComplaints.map { master -> master.chiefComplaint }.contains(it.toString())) {
                    itemData.chiefComplaint = it.toString()
                    updateResetButtonState()
                    itemChangeListener.onItemChanged()
                    chiefComplaintOptionInput.apply {
                        hintTextColor = defaultHintTextColor
                    }
                } else {
                    itemData.chiefComplaint = ""
                    chiefComplaintOptionInput.apply {
                        requestFocus()
                        boxStrokeColor = Color.RED
                        hintTextColor = ColorStateList.valueOf(Color.RED)
                    }
                }
            }

            durationInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isBinding) return
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) return

                    if (!s.isNullOrBlank() && s.length == 1 && s[0] == '0') s.clear()
                    if (!s.isNullOrBlank() && s.length > 2) {
                        s.replace(0, s.length, s.subSequence(0, 2))
                    }

                    itemList[position].duration = s.toString()
                    subtractButton.isEnabled = !s.isNullOrBlank() && (s.toString().toIntOrNull() ?: 0) > 1
                    updateResetButtonState()
                    itemChangeListener.onItemChanged()
                }
            })

            durationUnitDropdown.addTextChangedListener {
                if (isBinding) return@addTextChangedListener
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@addTextChangedListener
                itemList[position].durationUnit = it.toString()
                itemChangeListener.onItemChanged()
            }

            descriptionInput.addTextChangedListener {
                if (isBinding) return@addTextChangedListener
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@addTextChangedListener
                itemList[position].description = it.toString()
                updateResetButtonState()
                itemChangeListener.onItemChanged()
            }

            descInputLayout.setEndIconOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    endIconClickListener.onEndIconDescClick(position)
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

//        private fun deleteItem(position: Int) {
//            if (position in 0 until itemList.size) {
//                itemList.removeAt(position)
//                notifyItemRemoved(position)
//                itemChangeListener.onItemChanged()
//            }
//        }

        private fun deleteItem(position: Int) {
            if (position in 0 until itemList.size) {
                itemList.removeAt(position)
                if (itemList.isEmpty()) {
                    // Keep at least one complaint row visible after deletions.
                    itemList.add(ChiefComplaintValues())
                }
                // Rebind list to avoid stale row state after mid-list deletions.
                notifyDataSetChanged()
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
        holder.isBinding = true
        val itemData = itemList[position]
        holder.chiefComplaintOptions.setText(itemData.chiefComplaint, false)
        holder.durationInput.setText(itemData.duration)
        val selectedDurationUnit = itemData.durationUnit?.takeIf { it.isNotBlank() }
            ?: unitDropDown.getOrNull(1)
            ?: unitDropDown.firstOrNull()
            ?: ""
        holder.durationUnitDropdown.setText(selectedDurationUnit, false)
        holder.descriptionInput.setText(itemData.description)
        holder.cancelButton.isEnabled = itemCount > 1
        holder.subtractButton.isEnabled = (itemData.duration?.toIntOrNull() ?: 0) > 1

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
        holder.isBinding = false
        holder.updateResetButtonState()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.extra_chief_complaint_layout, parent, false)
        return ViewHolder(view)
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
