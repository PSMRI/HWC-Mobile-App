package org.piramalswasthya.cho.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.text.Html
import android.text.Spanned
import android.widget.Toast
import org.piramalswasthya.cho.R

class PatientListAdapter(context: Context, private val dataList: List<String>) :
    ArrayAdapter<String>(context, 0, dataList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var itemView = convertView
        val viewHolder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.patient_details_card, parent, false)
            viewHolder = ViewHolder(itemView)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }

        itemView?.setOnClickListener {
            // Handle the onclick event for the item at the given position
            Toast.makeText(context, "Clicked item at index $position", Toast.LENGTH_SHORT).show()
        }

        val customObject = dataList[position]
        viewHolder.bind(customObject)

        return itemView!!
    }

    private class ViewHolder(itemView: View) {

        private val name: TextView = itemView.findViewById(R.id.name)
        private val abhaNumber: TextView = itemView.findViewById(R.id.abha_number)
        private val age: TextView = itemView.findViewById(R.id.age)
        private val phoneNo: TextView = itemView.findViewById(R.id.phone_no)
        private val gender: TextView = itemView.findViewById(R.id.gender)

        var htmlString = "<b>Bold text</b>"
        var spannedText: Spanned = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)

        fun bind(customObject: String) {
            htmlString = "<b>Name: </b>" + "Dummy Name"
            spannedText = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
            name.text = spannedText

            htmlString = "<b>ABHA Number: </b>" + "HJDKLF"
            spannedText = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
            abhaNumber.text = spannedText

            htmlString = "<b>Age: </b>" + "24"
            spannedText = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
            age.text = spannedText

            htmlString = "<b>Phone No: </b>" + "8989898989"
            spannedText = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
            phoneNo.text = spannedText

            htmlString = "<b>Gender: </b>" + "Male"
            spannedText = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
            gender.text = spannedText
        }
    }
}

//class PatientListAdapter {
//
//}