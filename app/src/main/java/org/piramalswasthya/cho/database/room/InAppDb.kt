package org.piramalswasthya.cho.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.piramalswasthya.cho.database.converters.DateConverter
import org.piramalswasthya.cho.database.converters.DistrictBlockConverter
import org.piramalswasthya.cho.database.converters.DistrictConverter
import org.piramalswasthya.cho.database.converters.FaceVectorConvertor
import org.piramalswasthya.cho.database.converters.LocationConverter
import org.piramalswasthya.cho.database.converters.LocationEntityListConverter
import org.piramalswasthya.cho.database.converters.LoginSettingsDataConverter
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.converters.StateConverter
import org.piramalswasthya.cho.database.converters.StringConverters
import org.piramalswasthya.cho.database.converters.SyncStateConverter
import org.piramalswasthya.cho.database.converters.UserMasterLocationConverter
import org.piramalswasthya.cho.database.converters.VillageConverter
import org.piramalswasthya.cho.database.room.dao.BatchDao
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.CbacDao
import org.piramalswasthya.cho.database.room.dao.ChiefComplaintMasterDao
import org.piramalswasthya.cho.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.database.room.dao.EcrDao
import org.piramalswasthya.cho.database.room.dao.GovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.HealthCenterDao
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.database.room.dao.ImmunizationDao
import org.piramalswasthya.cho.database.room.dao.InvestigationDao
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.database.room.dao.LoginSettingsDataDao
import org.piramalswasthya.cho.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.database.room.dao.OtherGovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.OutreachDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.room.dao.PncDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionTemplateDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.ProcedureMasterDao
import org.piramalswasthya.cho.database.room.dao.ReferRevisitDao
import org.piramalswasthya.cho.database.room.dao.RegistrarMasterDataDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.SubCatVisitDao
import org.piramalswasthya.cho.database.room.dao.UserAuthDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.room.dao.VaccinationTypeAndDoseDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.moddel.OccupationMaster
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.AlcoholDropdown
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.AssociateAilmentsDropdown
import org.piramalswasthya.cho.model.AssociateAilmentsHistory
import org.piramalswasthya.cho.model.Batch
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.CbacCache
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.CommunityMaster
import org.piramalswasthya.cho.model.ComorbidConditionsDropdown
import org.piramalswasthya.cho.model.ComponentDataDownsync
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.ComponentDetailsMaster
import org.piramalswasthya.cho.model.ComponentOption
import org.piramalswasthya.cho.model.ComponentOptionsMaster
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.CovidVaccinationStatusHistory
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.DrugFormMaster
import org.piramalswasthya.cho.model.DrugFrequencyMaster
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.FamilyMemberDiseaseTypeDropdown
import org.piramalswasthya.cho.model.FamilyMemberDropdown
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.ImmunizationCache
import org.piramalswasthya.cho.model.IncomeMaster
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.LiteracyStatus
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.MaritalStatusMaster
import org.piramalswasthya.cho.model.MasterLocation
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.model.OutreachDropdownList
import org.piramalswasthya.cho.model.PNCVisitCache
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PastSurgeryHistory
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import org.piramalswasthya.cho.model.PregnantWomanRegistrationCache
import org.piramalswasthya.cho.model.PrescribedDrugs
import org.piramalswasthya.cho.model.PrescribedDrugsBatch
import org.piramalswasthya.cho.model.Prescription
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.PrescriptionWithItemMasterAndDrugFormMaster
import org.piramalswasthya.cho.model.Procedure
import org.piramalswasthya.cho.model.ProcedureDataDownsync
import org.piramalswasthya.cho.model.ProcedureMaster
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.QualificationMaster
import org.piramalswasthya.cho.model.ReferRevisitModel
import org.piramalswasthya.cho.model.RelationshipMaster
import org.piramalswasthya.cho.model.ReligionMaster
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.TobaccoAlcoholHistory
import org.piramalswasthya.cho.model.TobaccoDropdown
import org.piramalswasthya.cho.model.UserAuth
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.Vaccine
import org.piramalswasthya.cho.model.VaccineType
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VisitReason
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram

@Database(
    entities = [
        UserCache::class,
        LoginSettingsData::class,
        UserAuth::class,
        Language::class,
        VisitReason::class,
        VisitCategory::class,
        GenderMaster::class,
        AgeUnit::class,
        IncomeMaster::class,
        LiteracyStatus::class,
        CommunityMaster::class,
        MaritalStatusMaster::class,
        GovIdEntityMaster::class,
        OtherGovIdEntityMaster::class,
        RelationshipMaster::class,
        QualificationMaster::class,
        ReligionMaster::class,
        OccupationMaster::class,
        StateMaster::class,
        DoseType::class,
        VaccineType::class,
        SelectedOutreachProgram::class,
        DistrictMaster::class,
        BlockMaster::class,
        VillageMaster::class,
        ChiefComplaintMaster::class,
        SubVisitCategory:: class,
        IllnessDropdown::class,
        AlcoholDropdown::class,
        AllergicReactionDropdown::class,
        FamilyMemberDropdown::class,
        SurgeryDropdown::class,
        TobaccoDropdown::class,
        TobaccoAlcoholHistory::class,
        ComorbidConditionsDropdown::class,
        FamilyMemberDiseaseTypeDropdown::class,
        AssociateAilmentsDropdown::class,
        MedicationHistory::class,
        AssociateAilmentsHistory::class,
        ReferRevisitModel::class,
        Patient::class,
        HigherHealthCenter::class,
        PastSurgeryHistory::class,
        PastIllnessHistory::class,
        CovidVaccinationStatusHistory::class,
        InvestigationCaseRecord::class,
        PrescriptionCaseRecord::class,
        ChiefComplaintDB::class,
        ItemMasterList::class,
        Batch::class,
        DrugFrequencyMaster::class,
        CounsellingProvided::class,
        DrugFormMaster::class,
        ProceduresMasterData::class,
        BenFlow::class,
        PatientVitalsModel::class,
        DiagnosisCaseRecord::class,
        VisitDB::class,
        OutreachDropdownList::class,
        PatientVisitInfoSync::class,
        Procedure::class,
        ComponentDetails::class,
        ComponentOption::class,
        ProcedureDataDownsync::class,
        ComponentDataDownsync::class,
        Prescription::class,
        PrescribedDrugs::class,
        PrescribedDrugsBatch::class,
        MasterLocation::class,
        PregnantWomanAncCache::class,
        PregnantWomanRegistrationCache::class,
        PNCVisitCache::class,
        Vaccine::class,
        ImmunizationCache::class,
        DeliveryOutcomeCache::class,
        EligibleCoupleTrackingCache::class,
        PrescriptionTemplateDB::class,
        CbacCache::class,
        ProcedureMaster::class,
        ComponentDetailsMaster::class,
        ComponentOptionsMaster::class

    ],
    views = [PrescriptionWithItemMasterAndDrugFormMaster::class],
    version = 112, exportSchema = false
)


@TypeConverters(FaceVectorConvertor::class,
    LocationEntityListConverter::class,
    SyncStateConverter::class,
    StateConverter::class,
    LoginSettingsDataConverter::class,

    StateConverter::class,
    DistrictConverter::class,
    DistrictBlockConverter::class,
    VillageConverter::class,
    MasterDataListConverter::class,
    LocationConverter::class,
    DateConverter::class,
    UserMasterLocationConverter::class,
    StringConverters::class
)

abstract class InAppDb : RoomDatabase() {

    abstract val userDao: UserDao

    abstract val userAuthDao: UserAuthDao
    abstract val languageDao: LanguageDao
    abstract val stateMasterDao: StateMasterDao

    abstract val vaccinationTypeAndDoseDao: VaccinationTypeAndDoseDao
    abstract val visitReasonsAndCategoriesDao: VisitReasonsAndCategoriesDao
    abstract val registrarMasterDataDao:RegistrarMasterDataDao

    abstract val loginSettingsDataDao: LoginSettingsDataDao

    abstract val districtMasterDao: DistrictMasterDao
    abstract val blockMasterDao: BlockMasterDao
    abstract val villageMasterDao: VillageMasterDao

    abstract val govIdEntityMasterDao: GovIdEntityMasterDao
    abstract val otherGovIdEntityMasterDao: OtherGovIdEntityMasterDao
    abstract val chiefComplaintMasterDao: ChiefComplaintMasterDao
    abstract val subCatVisitDao: SubCatVisitDao
    abstract val historyDao: HistoryDao
    abstract val vitalsDao: VitalsDao
    abstract val referRevisitDao: ReferRevisitDao
    abstract val healthCenterDao: HealthCenterDao
    abstract val caseRecordeDao: CaseRecordeDao
    abstract val batchDao: BatchDao

    abstract val patientDao: PatientDao
    abstract val benFlowDao: BenFlowDao
    abstract val patientVisitInfoSyncDao: PatientVisitInfoSyncDao
    abstract val investigationDao: InvestigationDao
    abstract val prescriptionDao: PrescriptionDao
    abstract val outreachDao: OutreachDao
    abstract val procedureDao: ProcedureDao
    abstract val prescriptionTemplateDao:PrescriptionTemplateDao
    abstract val maternalHealthDao: MaternalHealthDao
    abstract val immunizationDao: ImmunizationDao
    abstract val deliveryOutcomeDao: DeliveryOutcomeDao
    abstract val pncDao: PncDao
    abstract val ecrDao: EcrDao
    abstract val cbacDao: CbacDao
    abstract val procedureMasterDao: ProcedureMasterDao

    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null
        val MIGRATION_106_107 = object : Migration(106, 107) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Patient ADD COLUMN faceEmbedding TEXT")
            }
        }

        val MIGRATION_107_108 = object : Migration(107, 108) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Patient ADD COLUMN instructions TEXT")
            }
        }

        val MIGRATION_108_109 = object : Migration(108, 109) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Investigation_Case_Record ADD COLUMN counsellingProvidedList TEXT")
            }
        }

        val MIGRATION_109_110 = object : Migration(109, 110) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS FP_DATA")
                database.execSQL("ALTER TABLE BenFlow ADD COLUMN externalInvestigation TEXT")
            }
        }

        val MIGRATION_110_111 = object : Migration(110, 111) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Seed lab procedure master data (laboratoryList from API response)
                val procedures = listOf(
                    Triple(101L, "Random Blood Glucose (RBS)", "Laboratory"),
                    Triple(104L, "RPR Card Test for Syphilis", "Laboratory"),
                    Triple(105L, "HIV-1 & HIV-2 (RDT)", "Laboratory"),
                    Triple(106L, "Serum Uric Acid", "Laboratory"),
                    Triple(107L, "HBsAg (RDT)", "Laboratory"),
                    Triple(108L, "Serum Total Cholesterol", "Laboratory"),
                    Triple(110L, "Hemoglobin", "Laboratory")
                )
                val components = listOf(
                    // testComponentID, range_normal_min, range_normal_max, range_min, range_max, isDecimal, inputType, measurementUnit, testComponentName, testComponentDesc
                    arrayOf(102L, 41, 140, 40, 500, 1, "TextBox", "mg/dl", "Random Blood Glucose (RBS)", "Random Blood Glucose (RBS)"),
                    arrayOf(105L, null, null, null, null, 0, "RadioButton", null, "RPR Card Test for Syphilis", "RPR Card Test for Syphilis"),
                    arrayOf(106L, null, null, null, null, 0, "RadioButton", null, "HIV-1 & HIV-2 (RDT)", "HIV-1 & HIV-2 (RDT)"),
                    arrayOf(107L, 3, 7, 0, 30, 1, "TextBox", "mg/dl", "Serum Uric Acid", "Serum Uric Acid"),
                    arrayOf(108L, null, null, null, null, 0, "RadioButton", null, "HBsAg (RDT)", "HBsAg (RDT)"),
                    arrayOf(109L, 100, 200, 99, 400, 1, "TextBox", "mg/dl", "Serum Total Cholesterol", "Serum Total Cholesterol"),
                    arrayOf(111L, 4, 15, 1, 18, 1, "TextBox", "g/dL", "Hemoglobin", "Hemoglobin")
                )
                val componentOptions = listOf(
                    emptyList(),
                    listOf("Negative", "Positive"),
                    listOf("Negative", "Positive"),
                    emptyList(),
                    listOf("Negative", "Positive"),
                    emptyList(),
                    emptyList()
                )
                for (i in procedures.indices) {
                    val (procId, name, procType) = procedures[i]
                    database.execSQL(
                        "INSERT INTO procedure_master (procedure_id, procedureDesc, procedureType, prescriptionID, procedureName, isMandatory) SELECT ?, ?, ?, 2802381, ?, 0 WHERE NOT EXISTS (SELECT 1 FROM procedure_master WHERE procedure_id = ?)",
                        arrayOf(procId, name, procType, name, procId)
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

        val MIGRATION_111_112 = object : Migration(111, 112) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove duplicate radio options (keep one Negative, one Positive per component)
                database.execSQL(
                    "DELETE FROM component_options_master WHERE id NOT IN (SELECT MIN(id) FROM component_options_master GROUP BY component_details_id, name)"
                )
            }
        }

        fun getInstance(appContext: Context): InAppDb {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        appContext,
                        InAppDb::class.java,
                        "CHO-1.0-In-app-database"
                    )
//                        .allowMainThreadQueries()
                        .addMigrations(
                            MIGRATION_106_107,
                            MIGRATION_107_108,
                            MIGRATION_108_109,
                            MIGRATION_109_110,
                            MIGRATION_110_111,
                            MIGRATION_111_112
                        )
                        .fallbackToDestructiveMigration()
                        .setQueryCallback(
                            object : QueryCallback {
                                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                                    // No-op: Query logging not required for this build.
                                }
                            },
                            Dispatchers.IO.asExecutor()
                        )
                        .build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}