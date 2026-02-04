package org.piramalswasthya.cho.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ChoListItemViewBinding
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.utils.DateTimeUtil

class CHOCaseRecordItemAdapter(
    private val clickListener: BenClickListener
) : ListAdapter<PatientDisplayWithVisitInfo, CHOCaseRecordItemAdapter.BenViewHolder>(
    BenDiffUtilCallBack
) {

    private var benFlowMap: Map<Int, BenFlow> = emptyMap()

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplayWithVisitInfo>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = oldItem.benVisitNo == newItem.benVisitNo

        override fun areContentsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = oldItem == newItem
    }

    class BenViewHolder private constructor(private val binding: ChoListItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChoListItemViewBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: PatientDisplayWithVisitInfo,
            clickListener: BenClickListener?,
            benFlow: BenFlow?
        ) {
            binding.benVisitInfo = item
            binding.clickListener = clickListener

            binding.itemll.setBackgroundColor(
                ContextCompat.getColor(
                    binding.itemll.context,
                    if (adapterPosition % 2 == 0) R.color.referBackground else R.color.text_secondary
                )
            )

            binding.visitNumber.text = item.benVisitNo?.toString() ?: ""
            val visitDateText = if (!benFlow?.visitDate.isNullOrBlank()) {
                DateTimeUtil.formatedDate(benFlow?.visitDate)
            } else {
                item.visitDate?.let { DateTimeUtil.formatDate(it) } ?: "N/A"
            }
            binding.visitDate.text = visitDateText

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        val item = getItem(position)
        val benFlow = item.benVisitNo?.let { benFlowMap[it] }
        holder.bind(item, clickListener, benFlow)
    }

    class BenClickListener(
        private val clickedBen: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
    ) {
        fun onClickedBen(item: PatientDisplayWithVisitInfo) = clickedBen(item)
    }

    fun updateBenFlows(newList: List<BenFlow>) {
        benFlowMap = newList.associateBy { it.benVisitNo ?: -1 }
        notifyDataSetChanged()
    }
}