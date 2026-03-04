package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.FormElement
import org.piramalswasthya.cho.model.InputType
import org.piramalswasthya.cho.model.ThroatDiagnosisAssessment

class ThroatDiagnosisDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: ThroatDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null

    /* -------------------- PAGE SETUP -------------------- */

    suspend fun setUpPage(savedRecord: ThroatDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        val list = mutableListOf<FormElement>()
        setUpPage(list)
    }


    /* -------------------- CACHE -------------------- */

    private fun createDefaultCache() =
        ThroatDiagnosisAssessment(patientID = "", benVisitNo = null)

    override suspend fun handleListOnValueChanged(
        formId: Int,
        index: Int
    ): Int {
        TODO("Not yet implemented")
    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as ThroatDiagnosisAssessment).apply {
        }
    }
}