package org.piramalswasthya.cho.database.room

/**
 * Constants for [org.piramalswasthya.cho.repositories.ProcedureRepo.ensureLabProcedureMasterSeed].
 * Procedure rows are taken from [org.piramalswasthya.cho.model.ProceduresMasterData] (Laboratory);
 * component rows are built from [org.piramalswasthya.cho.network.AmritApiService.getProcedureFields].
 */
object LabProcedureMasterSeed {

    const val PRESCRIPTION_ID = 2802381L

    /** Default radio options when the API does not return option lists (RadioButton). */
    val radioButtonDefaultOptions = listOf("Negative", "Positive")
}
