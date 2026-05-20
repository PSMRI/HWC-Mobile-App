package org.piramalswasthya.cho.ui.commons.fhir_visit_details

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
import org.piramalswasthya.cho.utils.MasterDataLocalizer

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
                itemChangeListener.onItemChanged()
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
                val ctx = chiefComplaintOptions.context
                val localized =
                    MasterDataLocalizer.localizeChiefComplaint(ctx, chiefComplaint.chiefComplaint)
                chiefComplaintOptions.setText(localized, false)
                itemList[position].id = chiefComplaint.chiefComplaintID
                itemList[position].chiefComplaint = chiefComplaint.chiefComplaint
            }

            chiefComplaintOptions.addTextChangedListener {
                if (isBinding) return@addTextChangedListener
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@addTextChangedListener
                val itemData = itemList[position]
                val typed = it.toString()
                val ctx = chiefComplaintOptions.context

                if (chiefComplaintOptionInput.error != null && typed.isNotBlank()) {
                    chiefComplaintOptionInput.error = null
                    chiefComplaintOptionInput.isErrorEnabled = false
                }

                // The user's input may be the localized label, the canonical English,
                // or free-typed text. Canonicalize first; if it resolves to a known
                // master value, accept it and store the canonical English.
                val canonical = MasterDataLocalizer.canonicalizeChiefComplaint(ctx, typed)
                val matchesMaster = canonical != null &&
                    chiefComplaints.any { master -> master.chiefComplaint == canonical }

                if (matchesMaster) {
                    itemData.chiefComplaint = canonical!!
                    updateResetButtonState()
                    itemChangeListener.onItemChanged()
                } else {
                    itemData.chiefComplaint = ""
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
                val typed = it.toString()
                val ctx = durationUnitDropdown.context
                // Convert localized display text back to canonical English for storage;
                // fall back to the raw input if it does not match a known unit.
                val canonical = MasterDataLocalizer.canonicalizeDurationUnit(ctx, typed) ?: typed
                itemList[position].durationUnit = canonical
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

        private fun deleteItem(position: Int) {
            if (itemList.size <= 1) return
            if (position !in 0 until itemList.size) return

            itemList.removeAt(position)
            notifyDataSetChanged()
            itemChangeListener.onItemChanged()
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
        val ctx = holder.itemView.context

        holder.chiefComplaintOptionInput.error = null
        holder.chiefComplaintOptionInput.isErrorEnabled = false

        // chiefComplaint is stored in canonical English; show the localized label.
        holder.chiefComplaintOptions.setText(
            MasterDataLocalizer.localizeChiefComplaint(ctx, itemData.chiefComplaint),
            false
        )
        holder.durationInput.setText(itemData.duration)

        val canonicalDurationUnit = itemData.durationUnit?.takeIf { it.isNotBlank() }
            ?: unitDropDown.getOrNull(1)
            ?: unitDropDown.firstOrNull()
            ?: ""
        holder.durationUnitDropdown.setText(
            MasterDataLocalizer.localizeDurationUnit(ctx, canonicalDurationUnit),
            false
        )

        holder.descriptionInput.setText(itemData.description)
        holder.cancelButton.isEnabled = itemCount > 1
        holder.subtractButton.isEnabled = (itemData.duration?.toIntOrNull() ?: 0) > 1

        // Render the duration-unit dropdown with localized labels; the TextWatcher
        // canonicalizes the displayed text back to English before persisting.
        val localizedUnits = unitDropDown.map { MasterDataLocalizer.localizeDurationUnit(ctx, it) }
        val unitDropdownAdapter =
            ArrayAdapter(ctx, R.layout.drop_down, localizedUnits)
        val chiefComplaintAdapter = ChiefComplaintAdapter(
            ctx,
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

    fun highlightEmptyChiefComplaints(rv: RecyclerView, message: String): Int {
        var firstEmpty = -1
        for (i in itemList.indices) {
            if (itemList[i].chiefComplaint.isNullOrEmpty()) {
                if (firstEmpty == -1) firstEmpty = i
                val vh = rv.findViewHolderForAdapterPosition(i) as? ViewHolder
                if (vh != null) {
                    vh.chiefComplaintOptionInput.isErrorEnabled = true
                    vh.chiefComplaintOptionInput.error = message
                } else {
                    val target = i
                    rv.scrollToPosition(target)
                    rv.post {
                        val late = rv.findViewHolderForAdapterPosition(target) as? ViewHolder
                        late?.chiefComplaintOptionInput?.let {
                            it.isErrorEnabled = true
                            it.error = message
                        }
                    }
                }
            }
        }
        return firstEmpty
    }

}

interface RecyclerViewItemChangeListener {
    fun onItemChanged()
}

interface EndIconClickListener {
    fun onEndIconDurationClick(position: Int)
    fun onEndIconDescClick(position: Int)
    fun onEndIconChiefClick(position: Int)
}
