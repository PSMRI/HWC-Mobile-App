package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import org.piramalswasthya.cho.R

class TempListAdapter(
    private val stringSet: HashSet<String?>,
    private val itemChangeListener: RecyclerViewItemClickedListener,
) :
    RecyclerView.Adapter<TempListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.templist_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = stringSet.elementAt(position)
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int = stringSet.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val text: TextView = itemView.findViewById(R.id.inputTestName)
        private val image: ImageButton = itemView.findViewById(R.id.dustbinIcon)

        fun bind(item: String?) {
            text.setText(item)
            image.setImageResource(R.drawable.dustbin_icon)
            image.setOnClickListener {
                   itemChangeListener.onItemClicked(item)
            }
        }
    }

}
interface RecyclerViewItemClickedListener {
    fun onItemClicked(item: String?)
}