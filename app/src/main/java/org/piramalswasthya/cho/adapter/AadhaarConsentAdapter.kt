package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvCardAbhaConcentBinding
import org.piramalswasthya.cho.model.AadhaarConsentModel

class AadhaarConsentAdapter(private val clickListener: ConsentClickListener) :
    ListAdapter<AadhaarConsentModel, AadhaarConsentAdapter.AadhaarConsentViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<AadhaarConsentModel>() {
        override fun areItemsTheSame(
            oldItem: AadhaarConsentModel, newItem: AadhaarConsentModel
        ) = oldItem.title == newItem.title

        override fun areContentsTheSame(
            oldItem: AadhaarConsentModel, newItem: AadhaarConsentModel
        ) = oldItem == newItem

    }

    class AadhaarConsentViewHolder private constructor(private val binding: RvCardAbhaConcentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): AadhaarConsentViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvCardAbhaConcentBinding.inflate(layoutInflater, parent, false)
                return AadhaarConsentViewHolder(binding)
            }
        }

        fun bind(
            item: AadhaarConsentModel, clickListener: ConsentClickListener
        ) {
            binding.item = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
//            This is check box
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked=item.checked
            binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                item.checked=isChecked
                if (isChecked){
                    clickListener.onClicked(item.apply {
                        checked = true },position)
                }else{
                    clickListener.onClicked(item.apply { checked = false },position)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= AadhaarConsentViewHolder.from(parent)

    override fun onBindViewHolder(holder: AadhaarConsentViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ConsentClickListener(val selectedListener: (selectedConsent: AadhaarConsentModel,position:Int) -> Unit) {
        fun onClicked(selectedConsent: AadhaarConsentModel,position:Int) = selectedListener(selectedConsent,position)
    }
}