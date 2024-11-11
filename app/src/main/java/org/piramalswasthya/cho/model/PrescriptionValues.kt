package org.piramalswasthya.cho.model

import androidx.room.Ignore
import java.io.Serializable

data class PrescriptionValues(
    var id: Int? = null,
    var form: String = "",
    var frequency: String = "",
    var dosage: String = "",
    var duration: String = "",
    var instruction: String = "",
    var unit: String = "",
    @Ignore
    var title: String = "Medicine-1"
) : Serializable
