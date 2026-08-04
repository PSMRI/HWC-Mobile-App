package org.piramalswasthya.cho.ui.commons

import android.os.Bundle
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.VisitMasterDb

object CphcFormNavigation {

    const val OTHER_CPHC_CATEGORY = "Other CPHC Services"

    fun buildVitalsBundle(
        arguments: Bundle?,
        subCategory: String,
        reasonForVisit: String? = null,
        category: String = OTHER_CPHC_CATEGORY,
        requireMasterDb: Boolean = false,
    ): Bundle {
        val masterDb = (arguments?.getSerializable("MasterDb") as? MasterDb)
            ?: if (requireMasterDb) {
                error("MasterDb is required but was not provided")
            } else {
                MasterDb(
                    patientId = arguments?.getString("patientID").orEmpty(),
                    visitMasterDb = VisitMasterDb(),
                )
            }

        masterDb.visitMasterDb?.apply {
            this.category = category
            this.subCategory = subCategory
            this.reason = reasonForVisit
                ?.takeIf { it.isNotBlank() }
                ?: arguments?.getString("reasonForVisit")?.takeIf { it.isNotBlank() }
                ?: this.reason?.takeIf { it.isNotBlank() }
                ?: subCategory
        }

        return Bundle().apply {
            putSerializable("MasterDb", masterDb)
            arguments?.getInt("benVisitNo", -1)?.takeIf { it > 0 }?.let { putInt("benVisitNo", it) }
        }
    }
}
