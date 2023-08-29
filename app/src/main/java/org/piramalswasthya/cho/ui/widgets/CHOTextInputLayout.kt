package org.piramalswasthya.cho.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.R

class CHOTextInputLayout  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {


    init {
        inflate(context, R.layout.cho_text_input_layout, this)

        val layout = findViewById<TextInputLayout>(R.id.cho_text_input_layout)

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CHOTextInputLayout,
            defStyleAttr,
            0
        )

//        setHint("hint");


        val textInputType = typedArray.getInt(R.styleable.CHOTextInputLayout_inputType, 1)
        val hintText = typedArray.getString(R.styleable.CHOTextInputLayout_hint)

        typedArray.recycle()

        layout.setHint(hintText)

        when(textInputType){
            1 -> {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
            }
            2 -> {
                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }
            3 -> {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
            }
            4 -> {
                editText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            else -> {

            }
        }

    }

    override fun getEditText(): TextInputEditText {
        return findViewById(R.id.cho_text_input_edit_text);
    }

}