package org.piramalswasthya.cho.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class SubCategoryAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    private val subCats: List<String>
) : ArrayAdapter<String>(context, resource, textViewResourceId, subCats) {

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            // Sub Category / Reason for Visit are pickers, not search fields.
            // Always return the full list so stale text (e.g. after navigating back)
            // doesn't narrow the dropdown to only the already-selected item.
            return FilterResults().apply {
                values = subCats
                count = subCats.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}
