package org.piramalswasthya.cho.utils

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import timber.log.Timber


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

@BindingAdapter("scope", "recordCount")
fun TextView.setRecordCount(scope: CoroutineScope, count: Flow<Int>?) {
    count?.let { flow ->
        scope.launch {
            try {
                flow.collect {
                    text = it.toString()
                }
            } catch (e: Exception) {
                Timber.d("Exception at record count : $e collected")
            }

        }
    } ?: run {
        text = null
    }
}

@BindingAdapter("allowRedBorder", "scope", "recordCount")
fun CardView.setRedBorder(allowRedBorder: Boolean, scope: CoroutineScope, count: Flow<Int>?) {
    count?.let {
        scope.launch {
            it.collect {
                if (it > 0 && allowRedBorder) {
                    setBackgroundResource(R.drawable.red_border)
                }
            }
        }
    }
}

@BindingAdapter("imageResource")
fun ImageView.setImageResourceBinding(resourceId: Int?) {
    resourceId?.let {
        setImageResource(it)
    }
}