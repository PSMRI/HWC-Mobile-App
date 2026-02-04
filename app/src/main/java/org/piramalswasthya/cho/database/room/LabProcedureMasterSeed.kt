package org.piramalswasthya.cho.database.room

import androidx.sqlite.db.SupportSQLiteDatabase

object LabProcedureMasterSeed {

    const val PRESCRIPTION_ID = 2802381L

    val procedures: List<Triple<Long, String, String>> = listOf(
        Triple(101L, "Random Blood Glucose (RBS)", "Laboratory"),
        Triple(104L, "RPR Card Test for Syphilis", "Laboratory"),
        Triple(105L, "HIV-1 & HIV-2 (RDT)", "Laboratory"),
        Triple(106L, "Serum Uric Acid", "Laboratory"),
        Triple(107L, "HBsAg (RDT)", "Laboratory"),
        Triple(108L, "Serum Total Cholesterol", "Laboratory"),
        Triple(110L, "Hemoglobin", "Laboratory")
    )

    val components: List<Array<Any?>> = listOf(
        arrayOf(102L, 41, 140, 40, 500, 1, "TextBox", "mg/dl", "Random Blood Glucose (RBS)", "Random Blood Glucose (RBS)"),
        arrayOf(105L, null, null, null, null, 0, "RadioButton", null, "RPR Card Test for Syphilis", "RPR Card Test for Syphilis"),
        arrayOf(106L, null, null, null, null, 0, "RadioButton", null, "HIV-1 & HIV-2 (RDT)", "HIV-1 & HIV-2 (RDT)"),
        arrayOf(107L, 3, 7, 0, 30, 1, "TextBox", "mg/dl", "Serum Uric Acid", "Serum Uric Acid"),
        arrayOf(108L, null, null, null, null, 0, "RadioButton", null, "HBsAg (RDT)", "HBsAg (RDT)"),
        arrayOf(109L, 100, 200, 99, 400, 1, "TextBox", "mg/dl", "Serum Total Cholesterol", "Serum Total Cholesterol"),
        arrayOf(111L, 4, 15, 1, 18, 1, "TextBox", "g/dL", "Hemoglobin", "Hemoglobin")
    )

    val componentOptions: List<List<String>> = listOf(
        emptyList(),
        listOf("Negative", "Positive"),
        listOf("Negative", "Positive"),
        emptyList(),
        listOf("Negative", "Positive"),
        emptyList(),
        emptyList()
    )

    fun runSeed(database: SupportSQLiteDatabase) {
        val prescriptionId = PRESCRIPTION_ID
        for (i in procedures.indices) {
            val (procId, name, procType) = procedures[i]
            database.execSQL(
                "INSERT INTO procedure_master (procedure_id, procedureDesc, procedureType, prescriptionID, procedureName, isMandatory) SELECT ?, ?, ?, ?, ?, 0 WHERE NOT EXISTS (SELECT 1 FROM procedure_master WHERE procedure_id = ?)",
                arrayOf(procId, name, procType, prescriptionId, name, procId)
            )
            val cursor = database.query("SELECT id FROM procedure_master WHERE procedure_id = ? ORDER BY id DESC LIMIT 1", arrayOf(procId))
            var masterProcId: Long = 0
            if (cursor.moveToFirst()) {
                masterProcId = cursor.getLong(0)
            }
            cursor.close()
            if (masterProcId == 0L) continue
            val comp = components[i]
            val testComponentId = comp[0]
            val rangeNormMin = comp[1]
            val rangeNormMax = comp[2]
            val rangeMin = comp[3]
            val rangeMax = comp[4]
            database.execSQL(
                "INSERT INTO component_details_master (test_component_id, procedure_id, range_normal_min, range_normal_max, range_min, range_max, isDecimal, inputType, measurement_nit, test_component_name, test_component_desc) SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM component_details_master WHERE procedure_id = ? AND test_component_id = ?)",
                arrayOf(testComponentId, masterProcId, rangeNormMin, rangeNormMax, rangeMin, rangeMax, comp[5], comp[6], comp[7], comp[8], comp[9], masterProcId, testComponentId)
            )
            val compCursor = database.query("SELECT id FROM component_details_master WHERE procedure_id = ? AND test_component_id = ? ORDER BY id DESC LIMIT 1", arrayOf(masterProcId, testComponentId))
            var compDetailsId: Long = 0
            if (compCursor.moveToFirst()) {
                compDetailsId = compCursor.getLong(0)
            }
            compCursor.close()
            for (optName in componentOptions[i]) {
                database.execSQL(
                    "INSERT OR IGNORE INTO component_options_master (component_details_id, name) VALUES (?, ?)",
                    arrayOf(compDetailsId, optName)
                )
            }
        }
    }
}
