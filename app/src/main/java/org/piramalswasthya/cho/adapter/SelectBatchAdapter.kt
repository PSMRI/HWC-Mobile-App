package org.piramalswasthya.cho.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.LayoutBatchBinding
import org.piramalswasthya.cho.model.PrescriptionBatchDTO
import org.piramalswasthya.cho.utils.DateTimeUtil

class SelectBatchAdapter(
    private val context: Context,
) : ListAdapter<PrescriptionBatchDTO, SelectBatchAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PrescriptionBatchDTO>() {
        override fun areItemsTheSame(
            oldItem: PrescriptionBatchDTO, newItem: PrescriptionBatchDTO
        ) = oldItem.batchNo == newItem.batchNo

        override fun areContentsTheSame(
            oldItem: PrescriptionBatchDTO, newItem: PrescriptionBatchDTO
        ) = oldItem == newItem

    }


    class BenViewHolder private constructor(private val binding: LayoutBatchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutBatchBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: PrescriptionBatchDTO,
        ) {
            binding.tvExpiryDate.text = DateTimeUtil.formatDateStr(item.expiryDate)
            binding.tvQuantityInHand.text = "In Hand Qty: ${item.qty.toString()}"
            binding.tvBatchNo.text = "Batch No: ${item.batchNo.toString()}"
            binding.checkboxSelect.setOnCheckedChangeListener(null)
            binding.checkboxSelect.isChecked = item.isSelected


            binding.etDispenseQuantity.doAfterTextChanged { text ->
                item.dispenseQuantity = text.toString().toIntOrNull() ?: 0
            }


            binding.checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
                if (isChecked) {
                    binding.etDispenseQuantity.requestFocus()
                } else {
                    item.dispenseQuantity = 0
                    binding.etDispenseQuantity.setText("")
                }
            }
            binding.etDispenseQuantity.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && !binding.checkboxSelect.isChecked) {
                    binding.checkboxSelect.isChecked = true
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position))

    }





}