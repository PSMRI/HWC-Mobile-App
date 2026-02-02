package org.piramalswasthya.cho.utils

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter


    @BindingAdapter("showLayout")
    fun Button.setVisibilityOfLayout(show: Boolean?) {
        show?.let {
            visibility = if (it) View.VISIBLE else View.GONE
        }
    }@BindingAdapter("showLayout")
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