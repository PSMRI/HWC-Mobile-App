package org.piramalswasthya.cho.utils

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.utils.DateTimeUtil
import timber.log.Timber
import java.util.Date

private const val TAG_RECORD_COUNT_JOB = 0x7f010000 // Arbitrary tag ID
private const val TAG_RED_BORDER_JOB = 0x7f010001 // Arbitrary tag ID for red border job


@BindingAdapter("showLayout")
fun Button.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("showLayout")
fun ImageView.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("showLayout")
fun LinearLayout.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("recordCount")
fun TextView.setRecordCount(count: Flow<Int>?) {
    // Cancel any previous collection job
    val previousJob = getTag(TAG_RECORD_COUNT_JOB) as? Job
    previousJob?.cancel()
    
    count?.let { flow ->
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            val job = lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        flow.collect {
                            text = it.toString()
                        }
                    } catch (e: Exception) {
                        Timber.d("Exception at record count : $e collected")
                    }
                }
            }
            setTag(TAG_RECORD_COUNT_JOB, job)
        } else {
            // Fallback: if lifecycle owner not available, set text to null
            text = null
        }
    } ?: run {
        text = null
    }
}

@BindingAdapter("allowRedBorder", "recordCount")
fun CardView.setRedBorder(allowRedBorder: Boolean, count: Flow<Int>?) {
    // Cancel any previous collection job
    val previousJob = getTag(TAG_RED_BORDER_JOB) as? Job
    previousJob?.cancel()
    
    count?.let { flow ->
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner != null) {
            val job = lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        flow.collect {
                            if (it > 0 && allowRedBorder) {
                                setBackgroundResource(R.drawable.red_border)
                            } else {
                                // Clear the red border when conditions aren't met
                                setBackgroundResource(0)
                            }
                        }
                    } catch (e: Exception) {
                        Timber.d("Exception at red border : $e collected")
                    }
                }
            }
            setTag(TAG_RED_BORDER_JOB, job)
        } else {
            // Fallback: if lifecycle owner not available, clear border
            setBackgroundResource(0)
        }
    } ?: run {
        // Clear border when count is null
        setBackgroundResource(0)
    }
}

@BindingAdapter("imageResource")
fun ImageView.setImageResourceBinding(resourceId: Int?) {
    resourceId?.let {
        setImageResource(it)
    }
}

@BindingAdapter("ageFromDate")
fun TextView.setAgeFromDate(dateOfBirth: Date?) {
    text = if (dateOfBirth != null) {
        DateTimeUtil.calculateAgeString(dateOfBirth)
    } else {
        "NA"
    }
}