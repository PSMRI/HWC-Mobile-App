package org.piramalswasthya.cho.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.DiagnosisValue
import org.piramalswasthya.cho.model.SnomedDiagnosis

class DiagnosisAdapter(
    private val mContext: Context,
    private val isVisitDetail: Boolean? = null,
    private val isFollowupVisit: Boolean? = null,
    private val itemList: MutableList<DiagnosisValue>,
    private var diagnosisList: List<SnomedDiagnosis>,
    private val itemChangeListener: RecyclerViewItemChangeListenerD
) : RecyclerView.Adapter<DiagnosisAdapter.ViewHolder>() {
    private val viewHolders = mutableListOf<ViewHolder>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diagnosisInput: AutoCompleteTextView = itemView.findViewById(R.id.inputDignosis)
        val diagnosisInputLayout: TextInputLayout = itemView.findViewById(R.id.diagnosis)
        val resetButton: FloatingActionButton = itemView.findViewById(R.id.resetButton)
        val cancelButton: FloatingActionButton = itemView.findViewById(R.id.deleteButton)
        var textWatcher: android.text.TextWatcher? = null
        var diagnosisAdapter: ArrayAdapter<String>? = null

        init {
            cancelButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && itemCount > 1) {
                    itemList.removeAt(position)
                    notifyItemRemoved(position)
                    itemChangeListener.onItemChanged()
                    updateDeleteButtonVisibility()
                }
            }
            resetButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemList[position].diagnosis = ""
                    notifyItemChanged(position)
                    itemChangeListener.onItemChanged()
                }
            }
        }

        fun updateResetButtonState() { resetButton.isEnabled = diagnosisInput.text.isNotEmpty() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.diagnosis_adapter_layout, parent, false)).also { viewHolders.add(it) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemData = itemList[position]
        holder.diagnosisAdapter = ArrayAdapter(mContext, android.R.layout.simple_dropdown_item_1line, diagnosisList.map { it.term })
        holder.diagnosisInput.setAdapter(holder.diagnosisAdapter)
        holder.textWatcher?.let(holder.diagnosisInput::removeTextChangedListener)
        holder.diagnosisInput.setText(itemData.diagnosis, false)
        holder.diagnosisInput.setOnItemClickListener { parent, _, selectedPosition, _ ->
            itemData.diagnosis = parent.getItemAtPosition(selectedPosition).toString()
            holder.diagnosisInput.setText(itemData.diagnosis, false)
            holder.diagnosisInput.setSelection(holder.diagnosisInput.length())
            holder.diagnosisInputLayout.error = null
            holder.updateResetButtonState()
            itemChangeListener.onItemChanged()
        }
        holder.diagnosisInput.setOnFocusChangeListener { _, hasFocus -> if (hasFocus && !holder.diagnosisInput.isReadOnly()) holder.diagnosisInput.showDropDown() }
        holder.textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString().orEmpty()
                if (text != itemData.diagnosis) itemData.diagnosis = ""
                holder.diagnosisInputLayout.error = if (text.isEmpty() || text == itemData.diagnosis) null else mContext.getString(R.string.provisional_diagnosis)
                holder.updateResetButtonState()
                itemChangeListener.onItemChanged()
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        }
        holder.diagnosisInput.addTextChangedListener(holder.textWatcher)

        val readOnly = isVisitDetail == true && isFollowupVisit == false || itemData.isPreFilled
        holder.itemView.visibility = if (isVisitDetail == true && isFollowupVisit == false && itemData.diagnosis.isBlank()) View.GONE else View.VISIBLE
        holder.resetButton.isVisible = !readOnly
        holder.cancelButton.isVisible = !readOnly
        holder.diagnosisInput.isFocusable = !readOnly
        holder.diagnosisInput.isClickable = !readOnly
        holder.diagnosisInputLayout.boxBackgroundColor = ContextCompat.getColor(mContext, if (readOnly) R.color.disable_field_color else R.color.white)
        holder.diagnosisInputLayout.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(mContext, if (readOnly) R.color.disable_field_hint_color else R.color.primaryTextColor))
        holder.cancelButton.isEnabled = itemCount > 1
        holder.updateResetButtonState()
    }

    private fun AutoCompleteTextView.isReadOnly() = !isFocusable
    private fun updateDeleteButtonVisibility() { viewHolders.forEach { it.cancelButton.isEnabled = itemCount > 1 } }
    override fun getItemCount() = itemList.size
    fun updateDiagnosisSuggestions(records: List<SnomedDiagnosis>) {
        diagnosisList = records
        val terms = records.map { it.term }
        viewHolders.forEach { holder ->
            holder.diagnosisAdapter?.apply {
                clear()
                addAll(terms)
                notifyDataSetChanged()
            }
        }
    }
    fun setError(): Int = itemList.indexOfFirst { it.diagnosis.isNullOrEmpty() }
}

interface RecyclerViewItemChangeListenerD { fun onItemChanged() }
