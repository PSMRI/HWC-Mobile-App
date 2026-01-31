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

    val lastClickTimeTag = "last_click_time"
    val dropdownStateTag = "dropdown_is_showing"
    val tagExistingClickListener = View.generateViewId()

    isFocusable = true
    isFocusableInTouchMode = true
    isClickable = true

    val existingClickListener = getTag(tagExistingClickListener) as? View.OnClickListener

    setOnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (isRapidClick(lastClickTimeTag.hashCode())) return@setOnTouchListener true

                hideKeyboardForSelfAndActivity()

                performClickSafely(view, existingClickListener)

                toggleDropdownState(dropdownStateTag.hashCode())

                return@setOnTouchListener true
            }
            MotionEvent.ACTION_DOWN -> {
                hideKeyboardForSelfAndActivity()
            }
            else -> Unit
        }
        false
    }

    setOnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            hideKeyboardForSelfAndActivity()
        } else {
            post {
                setTag(dropdownStateTag.hashCode(), isPopupShowing)
            }
        }
        existingFocusListener?.onFocusChange(view, hasFocus)
    }
}

// --- Small, focused helpers to reduce cognitive complexity of the public method ---
private fun AutoCompleteTextView.hideKeyboardForSelfAndActivity() {
    KeyboardUtils.hideKeyboard(this)
    KeyboardUtils.hideKeyboardFromActivity(context)
}

private fun AutoCompleteTextView.isRapidClick(lastClickTimeTagHash: Int): Boolean {
    val currentTime = System.currentTimeMillis()
    val lastClickTime = getTag(lastClickTimeTagHash) as? Long ?: 0L
    if (currentTime - lastClickTime < 300) return true
    setTag(lastClickTimeTagHash, currentTime)
    return false
}

private fun AutoCompleteTextView.performClickSafely(view: View, existingClickListener: View.OnClickListener?) {
    existingClickListener?.onClick(view)
    view.performClick()
}

private fun AutoCompleteTextView.toggleDropdownState(dropdownStateTagHash: Int) {
    if (adapter == null || adapter.count == 0) return
    val isDropdownShowing = getTag(dropdownStateTagHash) as? Boolean ?: false
    val actualPopupShowing = isPopupShowing
    if (isDropdownShowing || actualPopupShowing) {
        dismissDropDown()
        setTag(dropdownStateTagHash, false)
    } else {
        showDropDown()
        setTag(dropdownStateTagHash, true)
    }
}

