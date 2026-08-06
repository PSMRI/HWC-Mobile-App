package org.piramalswasthya.cho.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.model.Patient

/**
 * Utility functions for RMNCHA+ Activities to reduce code duplication
 */

/**
 * Extension function to filter patients by common search fields
 * Searches in: firstName, lastName, spouseName, phoneNo, beneficiaryID
 */
fun <T : Any> List<T>.filterPatientsByQuery(
    query: String,
    patientExtractor: (T) -> Patient
): List<T> {
    if (query.isBlank()) return this

    val searchQuery = query.lowercase()
    return filter { item ->
        val patient = patientExtractor(item)
        val firstName = patient.firstName?.lowercase() ?: ""
        val lastName = patient.lastName?.lowercase() ?: ""
        val spouseName = patient.spouseName?.lowercase() ?: ""
        val phoneNo = patient.phoneNo ?: ""
        val beneficiaryID = patient.beneficiaryID?.toString() ?: ""

        firstName.contains(searchQuery) ||
                lastName.contains(searchQuery) ||
                spouseName.contains(searchQuery) ||
                phoneNo.contains(searchQuery) ||
                beneficiaryID.contains(searchQuery)
    }
}

/**
 * Extension function to setup search TextWatcher
 */
fun EditText.setupSearchTextWatcher(onTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No action needed before text changes
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No action needed during text changes - handled in afterTextChanged
        }

        override fun afterTextChanged(s: Editable?) {
            onTextChanged(s?.toString() ?: "")
        }
    })
}

/**
 * Extension function to update UI with filtered list
 * Handles empty state, count display, and RecyclerView visibility
 */
fun <T> updateListUI(
    filteredList: List<T>,
    emptyStateView: FrameLayout,
    recyclerView: RecyclerView,
    countTextView: TextView,
    resultString: String,
    logMessage: String = ""
) {
    if (filteredList.isEmpty()) {
        emptyStateView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        countTextView.text = "0 $resultString"
    } else {
        emptyStateView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        val countText = if (filteredList.size == 1) {
            "1 $resultString"
        } else {
            "${filteredList.size} ${resultString}s"
        }
        countTextView.text = countText
    }

    if (logMessage.isNotEmpty()) {
        timber.log.Timber.d("$logMessage: ${filteredList.size}")
    }
}

/**
 * Extension function to setup toolbar with back button
 */
fun androidx.appcompat.app.AppCompatActivity.setupToolbarWithBack(
    toolbar: androidx.appcompat.widget.Toolbar,
    title: String
) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = title
}

/**
 * Helper function to get result string resource
 */
fun getResultString(count: Int, resultString: String): String {
    return if (count == 1) {
        "1 $resultString"
    } else {
        "$count ${resultString}s"
    }
}
