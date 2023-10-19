package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
//import org.piramalswasthya.cho.model.BenWithAncListDomain
import org.piramalswasthya.cho.databinding.RvItemPregnancyVisitBinding

//class AncVisitListAdapter(private val clickListener: PregnancyVisitClickListener? = null) :
//    ListAdapter<BenWithAncListDomain, AncVisitListAdapter.PregnancyVisitViewHolder>(
//        MyDiffUtilCallBack
//    ) {
//    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<BenWithAncListDomain>() {
//        override fun areItemsTheSame(
//            oldItem: BenWithAncListDomain, newItem: BenWithAncListDomain
//        ) = oldItem.ben.benId == newItem.ben.benId
//
//        override fun areContentsTheSame(
//            oldItem: BenWithAncListDomain, newItem: BenWithAncListDomain
//        ) = oldItem == newItem
//
//    }
//
//    class PregnancyVisitViewHolder private constructor(private val binding: RvItemPregnancyVisitBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        companion object {
//            fun from(parent: ViewGroup): PregnancyVisitViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = RvItemPregnancyVisitBinding.inflate(layoutInflater, parent, false)
//                return PregnancyVisitViewHolder(binding)
//            }
//        }
//
//        fun bind(
//            item: BenWithAncListDomain, clickListener: PregnancyVisitClickListener?
//        ) {
//            binding.visit = item
//            binding.btnAddAnc.visibility = if (item.showAddAnc) View.VISIBLE else View.INVISIBLE
//            binding.btnPmsma.visibility = if (item.pmsmaFillable) View.VISIBLE else View.INVISIBLE
//            binding.btnPmsma.text = if (item.hasPmsma) "View PMSMA" else "Add PMSMA"
//            binding.btnPmsma.setBackgroundColor(binding.root.resources.getColor(if (item.hasPmsma) android.R.color.holo_green_dark else android.R.color.holo_red_dark))
//            binding.btnViewVisits.visibility =
//                if (item.anc.isEmpty()) View.INVISIBLE else View.VISIBLE
//            binding.clickListener = clickListener
//            binding.executePendingBindings()
//
//        }
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup, viewType: Int
//    ) = PregnancyVisitViewHolder.from(parent)
//
//    override fun onBindViewHolder(holder: PregnancyVisitViewHolder, position: Int) {
//        holder.bind(getItem(position), clickListener)
//    }
//
//
//    class PregnancyVisitClickListener(
//        private val showVisits: (benId: Long) -> Unit,
//        private val addVisit: (benId: Long, visitNumber: Int) -> Unit,
//        private val pmsma: (benId: Long, hhId : Long) -> Unit,
//
//        ) {
//        fun showVisits(item: BenWithAncListDomain) = showVisits(
//            item.ben.benId,
//        )
//
//        fun addVisit(item: BenWithAncListDomain) = addVisit(item.ben.benId,
//            if (item.anc.isEmpty()) 1 else item.anc.maxOf { it.visitNumber } + 1)
//
//        fun pmsma(item: BenWithAncListDomain) = pmsma(
//            item.ben.benId, item.ben.hhId
//        )
//    }
//
//}