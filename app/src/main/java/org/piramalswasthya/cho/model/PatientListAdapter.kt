package org.piramalswasthya.cho.model

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.text.Html
import android.text.Spanned
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity

class PatientListAdapter(context: Context, private val dataList: List<PatientDetails>) :
    ArrayAdapter<PatientDetails>(context, 0, dataList) {

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
//            val intent = Intent(context, EditPatientDetailsActivity::class.java)
//            intent.putExtra("key", "value")
//            startActivity(intent)

            val intent = Intent(context, EditPatientDetailsActivity::class.java)
//            val gson = Gson()
//            val patientDetailsGson = gson.toJson(dataList[position])
//            intent.putExtra("patientDetails", patientDetailsGson)
            intent.putExtra("index", position)
            startActivity(context, intent, null)

//            Toast.makeText(context, "Clicked item at index $position", Toast.LENGTH_SHORT).show()
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

        fun bind(customObject: PatientDetails) {
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