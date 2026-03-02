package org.piramalswasthya.cho.configuration

import android.content.Context
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import org.piramalswasthya.cho.model.FormElement

class NoseDiagnosisDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private lateinit var cache: NoseDiagnosisAssessment

    var onShowAlert: ((String) -> Unit)? = null


    suspend fun setUpPage(savedRecord: NoseDiagnosisAssessment?) {
        cache = savedRecord ?: createDefaultCache()
        val list = mutableListOf<FormElement>()
        setUpPage(list)
    }




    private fun createDefaultCache(): NoseDiagnosisAssessment {
        return NoseDiagnosisAssessment(
            patientID = "",
            benVisitNo = null
        )
    }

    override suspend fun handleListOnValueChanged(
        formId: Int,
        index: Int
    ): Int {
        TODO("Not yet implemented")
    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as NoseDiagnosisAssessment).let {

        }
    }
}
