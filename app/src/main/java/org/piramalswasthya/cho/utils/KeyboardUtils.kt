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

    setOnTouchListener {_, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            KeyboardUtils.hideKeyboard(this)
            KeyboardUtils.hideKeyboardFromActivity(this.context)
        }
        false
    }
    
    setOnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            KeyboardUtils.hideKeyboard(this)
            KeyboardUtils.hideKeyboardFromActivity(this.context)
            
            postDelayed({
                if (isPopupShowing) {
                    KeyboardUtils.hideKeyboard(this)
                    KeyboardUtils.hideKeyboardFromActivity(this.context)
                }
            }, 100)
        }
        existingFocusListener?.onFocusChange(view, hasFocus)
    }

    val existingClickTag = getTag(android.R.id.text1)
    setOnClickListener { view ->
        KeyboardUtils.hideKeyboard(this)
        KeyboardUtils.hideKeyboardFromActivity(this.context)
        (existingClickTag as? View.OnClickListener)?.onClick(view)
    }
}
