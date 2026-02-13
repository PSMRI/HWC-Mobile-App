package org.piramalswasthya.cho.database.room

/**
 * Seed data for lab procedure master. Seeding is done via [ProcedureRepo.ensureLabProcedureMasterSeed]
 * using Room DAO inserts (avoids raw SQL binding issues with procedureDesc NOT NULL).
 */
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
}
