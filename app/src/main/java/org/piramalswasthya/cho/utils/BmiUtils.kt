package org.piramalswasthya.cho.utils

import android.content.Context
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import org.piramalswasthya.cho.R
import kotlin.math.pow

object BmiUtils {

    fun applyBmiCategory(
        context: Context,
        bmiValue: String?,
        bmiCategoryView: TextView,
        bmiInputView: TextInputEditText
    ) {
        val bmi = bmiValue?.trim()?.toFloatOrNull()
        if (bmi == null || bmi <= 0f) {
            bmiCategoryView.isVisible = false
            bmiInputView.setTextColor(context.resources.getColor(R.color.black))
            return
        }

        bmiCategoryView.isVisible = true
        when {
            bmi > 30f -> {
                bmiCategoryView.text = context.getString(R.string.obese_txt)
                bmiCategoryView.setTextColor(context.resources.getColor(R.color.red))
//                bmiInputView.setTextColor(context.resources.getColor(R.color.red))
            }
            bmi > 25f -> {
                bmiCategoryView.text = context.getString(R.string.overweight_txt)
                bmiCategoryView.setTextColor(context.resources.getColor(R.color.red))
//                bmiInputView.setTextColor(context.resources.getColor(R.color.red))
            }
            else -> {
                bmiCategoryView.text = context.getString(R.string.normal_txt)
                bmiCategoryView.setTextColor(context.resources.getColor(R.color.green))
//                bmiInputView.setTextColor(context.resources.getColor(R.color.black))
            }
        }
    }

    fun applyBmiCategoryFromAnthropometry(
        context: Context,
        heightCm: String?,
        weightKg: String?,
        bmiText: String?,
        bmiCategoryView: TextView,
        bmiInputView: TextInputEditText
    ) {
        val bmiFromField = bmiText?.trim()?.toFloatOrNull()
        val height = heightCm?.trim()?.toFloatOrNull()
        val weight = weightKg?.trim()?.toFloatOrNull()
        val bmi = bmiFromField ?: if (height != null && weight != null && height > 0f) {
            weight / (height / 100f).pow(2)
        } else {
            null
        }

        applyBmiCategory(
            context,
            bmi?.let { "%.2f".format(it) },
            bmiCategoryView,
            bmiInputView
        )
    }
}
