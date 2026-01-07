package org.piramalswasthya.cho.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView

object KeyboardUtils {

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        view.windowToken?.let {
            imm?.hideSoftInputFromWindow(it, InputMethodManager.HIDE_NOT_ALWAYS)
            imm?.hideSoftInputFromWindow(it, 0)
        }
        view.rootView?.let { rootView ->
            rootView.windowToken?.let {
                imm?.hideSoftInputFromWindow(it, InputMethodManager.HIDE_NOT_ALWAYS)
                imm?.hideSoftInputFromWindow(it, 0)
            }
        }
        val activity = (view.context as? android.app.Activity)
        activity?.currentFocus?.windowToken?.let {
            imm?.hideSoftInputFromWindow(it, InputMethodManager.HIDE_NOT_ALWAYS)
            imm?.hideSoftInputFromWindow(it, 0)
        }
    }

    fun hideKeyboardFromActivity(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val activity = (context as? android.app.Activity) ?: return
        val view = activity.currentFocus ?: activity.window.decorView.rootView
        view.windowToken?.let {
            imm?.hideSoftInputFromWindow(it, InputMethodManager.HIDE_NOT_ALWAYS)
            imm?.hideSoftInputFromWindow(it, 0)
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun AutoCompleteTextView.setupDropdownKeyboardHandling() {
    val existingFocusListener = onFocusChangeListener

    showSoftInputOnFocus = false
    
    val LAST_CLICK_TIME_TAG = "last_click_time"
    val DROPDOWN_STATE_TAG = "dropdown_is_showing"
    
    isFocusable = true
    isFocusableInTouchMode = true
    isClickable = true
    
    val existingClickListener = getTag(android.R.id.text1) as? View.OnClickListener

    setOnTouchListener { view, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val currentTime = System.currentTimeMillis()
            val lastClickTime = getTag(LAST_CLICK_TIME_TAG.hashCode()) as? Long ?: 0L
            
            if (currentTime - lastClickTime < 300) {
                return@setOnTouchListener true
            }
            setTag(LAST_CLICK_TIME_TAG.hashCode(), currentTime)
            
            KeyboardUtils.hideKeyboard(this)
            KeyboardUtils.hideKeyboardFromActivity(this.context)
            
            existingClickListener?.onClick(view)
            
            if (adapter != null && adapter.count > 0) {
                val isDropdownShowing = getTag(DROPDOWN_STATE_TAG.hashCode()) as? Boolean ?: false
                val actualPopupShowing = isPopupShowing

                if (isDropdownShowing || actualPopupShowing) {
                    dismissDropDown()
                    setTag(DROPDOWN_STATE_TAG.hashCode(), false)
                } else {
                    showDropDown()
                    setTag(DROPDOWN_STATE_TAG.hashCode(), true)
                }
            }
            
            return@setOnTouchListener true
        } else if (event.action == MotionEvent.ACTION_DOWN) {
            KeyboardUtils.hideKeyboard(this)
            KeyboardUtils.hideKeyboardFromActivity(this.context)
        }
        false
    }
    
    setOnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            KeyboardUtils.hideKeyboard(this)
            KeyboardUtils.hideKeyboardFromActivity(this.context)
        } else {
            post {
                val actualState = isPopupShowing
                setTag(DROPDOWN_STATE_TAG.hashCode(), actualState)
            }
        }
        existingFocusListener?.onFocusChange(view, hasFocus)
    }
}
