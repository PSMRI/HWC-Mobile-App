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
import org.piramalswasthya.cho.database.room.dao.NeonatalOutcomeDao
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.database.room.dao.EcrDao
import org.piramalswasthya.cho.database.room.dao.GovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.HealthCenterDao
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.database.room.dao.ImmunizationDao
import org.piramalswasthya.cho.database.room.dao.InfantRegDao
import org.piramalswasthya.cho.database.room.dao.InvestigationDao
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.database.room.dao.LoginSettingsDataDao
import org.piramalswasthya.cho.database.room.dao.AshaDueListDao
import org.piramalswasthya.cho.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.database.room.dao.OphthalmicDao
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
import org.piramalswasthya.cho.database.room.dao.StatusOfWomanDao
import org.piramalswasthya.cho.database.room.dao.SubCatVisitDao
import org.piramalswasthya.cho.database.room.dao.UserAuthDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.room.dao.VaccinationTypeAndDoseDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.database.room.dao.EarDiagnosisAssessmentDao
import org.piramalswasthya.cho.database.room.dao.NoseDiagnosisAssessmentDao
import org.piramalswasthya.cho.database.room.dao.PainAndSymptomAssessmentDao
import org.piramalswasthya.cho.database.room.dao.OralHealthDao
import org.piramalswasthya.cho.database.room.dao.PsychosocialCaregiverSupportDao
import org.piramalswasthya.cho.moddel.OccupationMaster
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.AshaDueListCache
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
import org.piramalswasthya.cho.model.EligibleCoupleRegCache
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.NeonatalOutcomeCache
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
import org.piramalswasthya.cho.model.OphthalmicVisit
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
import org.piramalswasthya.cho.model.StatusOfWomanMaster
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
import org.piramalswasthya.cho.model.EarDiagnosisAssessment
import org.piramalswasthya.cho.model.NoseDiagnosisAssessment
import org.piramalswasthya.cho.model.PainAndSymptomAssessment
import org.piramalswasthya.cho.model.OralHealth
import org.piramalswasthya.cho.model.PsychosocialCaregiverSupport


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
        NeonatalOutcomeCache::class,
        EligibleCoupleRegCache::class,
        EligibleCoupleTrackingCache::class,
        InfantRegCache::class,
        PrescriptionTemplateDB::class,
        CbacCache::class,
        ProcedureMaster::class,
        ComponentDetailsMaster::class,
        ComponentOptionsMaster::class,
        AshaDueListCache::class,
        StatusOfWomanMaster::class,
        OphthalmicVisit::class,
        EarDiagnosisAssessment::class,
        NoseDiagnosisAssessment::class,
        PainAndSymptomAssessment::class,
        PsychosocialCaregiverSupport::class,
        OralHealth::class
    ],
    views = [PrescriptionWithItemMasterAndDrugFormMaster::class],
    version = 132, exportSchema = false
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
    abstract val ashaDueListDao: AshaDueListDao
    abstract val immunizationDao: ImmunizationDao
    abstract val deliveryOutcomeDao: DeliveryOutcomeDao
    abstract val neonatalOutcomeDao: NeonatalOutcomeDao
    abstract val pncDao: PncDao
    abstract val ecrDao: EcrDao
    abstract val infantRegDao: InfantRegDao
    abstract val cbacDao: CbacDao
    abstract val procedureMasterDao: ProcedureMasterDao
    abstract val painAndSymptomAssessmentDao: PainAndSymptomAssessmentDao
    abstract val psychosocialCaregiverSupportDao: PsychosocialCaregiverSupportDao


    // This comment is for Github glitch
    abstract val statusOfWomanDao: StatusOfWomanDao
    abstract val ophthalmicDao: OphthalmicDao
    abstract val earDiagnosisAssessmentDao: EarDiagnosisAssessmentDao
    abstract val oralHealthDao: OralHealthDao
    abstract val noseDiagnosisAssessmentDao: NoseDiagnosisAssessmentDao

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
                // Lab procedure master seed is now applied via ProcedureRepo.ensureLabProcedureMasterSeed() (DAO) when user opens lab technician
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

        //        There was conflict i have fixed and inform to Abhilash and shiva to verify
        val MIGRATION_112_113 = object : Migration(112, 113) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ELIGIBLE_COUPLE_REG (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patientID TEXT NOT NULL,
                        dateOfReg INTEGER NOT NULL,
                        lmpDate INTEGER,
                        noOfChildren INTEGER NOT NULL,
                        noOfLiveChildren INTEGER NOT NULL,
                        noOfMaleChildren INTEGER NOT NULL,
                        noOfFemaleChildren INTEGER NOT NULL,
                        isRegistered INTEGER NOT NULL,
                        processed TEXT,
                        createdBy TEXT NOT NULL,
                        createdDate INTEGER NOT NULL,
                        updatedBy TEXT NOT NULL,
                        updatedDate INTEGER NOT NULL,
                        syncState INTEGER NOT NULL,
                        FOREIGN KEY(patientID) REFERENCES PATIENT(patientID) ON UPDATE CASCADE ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS ecrInd ON ELIGIBLE_COUPLE_REG(patientID)")
            }
        }

        val MIGRATION_113_114 = object : Migration(113, 114) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS INFANT_REG (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        childPatientID TEXT,
                        motherPatientID TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        babyName TEXT,
                        babyIndex INTEGER NOT NULL,
                        infantTerm TEXT,
                        corticosteroidGiven TEXT,
                        genderID INTEGER,
                        babyCriedAtBirth INTEGER,
                        resuscitation INTEGER,
                        referred TEXT,
                        hadBirthDefect TEXT,
                        birthDefect TEXT,
                        otherDefect TEXT,
                        weight REAL,
                        breastFeedingStarted INTEGER,
                        opv0Dose INTEGER,
                        bcgDose INTEGER,
                        hepBDose INTEGER,
                        vitkDose INTEGER,
                        processed TEXT,
                        createdBy TEXT NOT NULL,
                        createdDate INTEGER NOT NULL,
                        updatedBy TEXT NOT NULL,
                        updatedDate INTEGER NOT NULL,
                        syncState INTEGER NOT NULL,
                        FOREIGN KEY(motherPatientID) REFERENCES PATIENT(patientID) ON UPDATE CASCADE ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS infRegInd ON INFANT_REG(motherPatientID)")
            }
        }

        val MIGRATION_114_115 = object : Migration(114, 115) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate component_details_master so FK references procedure_master(id) instead of procedure(id).
                database.execSQL("PRAGMA foreign_keys=OFF")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS component_details_master_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        test_component_id INTEGER NOT NULL,
                        procedure_id INTEGER NOT NULL,
                        range_normal_min INTEGER,
                        range_normal_max INTEGER,
                        range_min INTEGER,
                        range_max INTEGER,
                        isDecimal INTEGER,
                        inputType TEXT NOT NULL,
                        measurement_nit TEXT,
                        test_component_name TEXT NOT NULL,
                        test_component_desc TEXT NOT NULL,
                        FOREIGN KEY(procedure_id) REFERENCES procedure_master(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO component_details_master_new (
                        id, test_component_id, procedure_id, range_normal_min, range_normal_max,
                        range_min, range_max, isDecimal, inputType, measurement_nit,
                        test_component_name, test_component_desc
                    ) SELECT
                        id, test_component_id, procedure_id, range_normal_min, range_normal_max,
                        range_min, range_max, isDecimal, inputType, measurement_nit,
                        test_component_name, test_component_desc
                    FROM component_details_master
                """.trimIndent())
                database.execSQL("DROP TABLE component_details_master")
                database.execSQL("ALTER TABLE component_details_master_new RENAME TO component_details_master")
                database.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        val MIGRATION_115_116 = object : Migration(115, 116) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to ELIGIBLE_COUPLE_TRACKING table
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN financialYear TEXT")
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN visitMonth TEXT")
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN lmpDate INTEGER")
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN anyOtherMethod TEXT")
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN antraDose TEXT")
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN antraInjectionDate INTEGER")
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN antraDueDate INTEGER")
                database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN dateOfSterilization INTEGER")
            }
        }

        val MIGRATION_116_117 = object : Migration(116, 117) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ASHA_DUE_LIST (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patientID TEXT NOT NULL,
                        beneficiaryID INTEGER,
                        listType TEXT NOT NULL DEFAULT 'ANC',
                        addedDate INTEGER NOT NULL,
                        ashaId INTEGER NOT NULL DEFAULT 0,
                        createdBy TEXT NOT NULL,
                        syncState INTEGER NOT NULL,
                        FOREIGN KEY(patientID) REFERENCES PATIENT(patientID) ON UPDATE CASCADE ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS ind_asha_due ON ASHA_DUE_LIST(patientID, listType)")
            }
        }

        val MIGRATION_117_118 = object : Migration(117, 118) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP INDEX IF EXISTS ind_asha_due")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ind_asha_due ON ASHA_DUE_LIST(patientID, listType)")
            }
        }

        val MIGRATION_118_119 = object : Migration(118, 119) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN motherCondition TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN maternalComplications TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN motherCurrentlyAdmitted INTEGER")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeath INTEGER")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeathValue TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN dateOfDeath TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeath TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeathId INTEGER")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN otherPlaceOfDeath TEXT")
            }
        }

        val MIGRATION_119_120 = object : Migration(119, 120) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to PATIENT table
                database.execSQL("ALTER TABLE PATIENT ADD COLUMN statusOfWomanID INTEGER")

                // Create STATUS_OF_WOMAN_MASTER table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS STATUS_OF_WOMAN_MASTER (
                        statusID INTEGER PRIMARY KEY NOT NULL,
                        statusName TEXT NOT NULL,
                        statusCode TEXT NOT NULL
                    )
                """)

                // Insert default status values
                database.execSQL("INSERT INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (1, 'Eligible Couple', 'EC')")
                database.execSQL("INSERT INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (2, 'Pregnant Woman', 'PW')")
                database.execSQL("INSERT INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (3, 'Postnatal', 'PN')")
                database.execSQL("INSERT INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (4, 'Elderly', 'EL')")
                database.execSQL("INSERT INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (5, 'Adolescent', 'AD')")
                database.execSQL("INSERT INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (6, 'Permanent Sterilization', 'ST')")
                database.execSQL("INSERT INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (7, 'Not Applicable', 'NA')")
            }
        }

        val MIGRATION_120_121 = object : Migration(120, 121) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add missing ANC tracking fields from FLW app
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN lmpDate INTEGER")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN visitDate INTEGER")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN weekOfPregnancy INTEGER")

                // Abortion extended fields
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN serialNo TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN methodOfTermination TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN methodOfTerminationId INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN terminationDoneBy TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN terminationDoneById INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN isPaiucdId INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN isYesOrNo INTEGER")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN isPaiucd TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN dateSterilisation INTEGER")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN remarks TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN abortionImg1 TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN abortionImg2 TEXT")

                // Maternal death extended fields
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfDeath TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfDeathId INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN otherPlaceOfDeath TEXT")

                // MCP card image paths
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN frontFilePath TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN backFilePath TEXT")
            }
        }

        val MIGRATION_121_122 = object : Migration(121, 122) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add JIRA validation requirement fields
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN bloodSugarFasting INTEGER")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN urineSugar TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN urineSugarId INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN fetalHeartRate REAL")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN calciumGiven INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN dangerSigns TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN dangerSignsId INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN counsellingProvided INTEGER")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN counsellingTopics TEXT")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN counsellingTopicsId INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN nextAncVisitDate INTEGER")
            }
        }

        val MIGRATION_122_123 = object : Migration(122, 123) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN gestationalAgeAtDelivery TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN deliveryConductedBy TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN modeOfDelivery TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN indicationForLSCS TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN indicationForLSCSOther TEXT")
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN privateHospitalName TEXT")
            }
        }

        val MIGRATION_123_124 = object : Migration(123, 124) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE PREGNANCY_REGISTER ADD COLUMN isFirstAncSubmitted INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE PREGNANCY_REGISTER ADD COLUMN historyOfAbortions INTEGER"
                )
                db.execSQL(
                    "ALTER TABLE PREGNANCY_REGISTER ADD COLUMN previousLSCS INTEGER"
                )
            }
        }


        val MIGRATION_124_125 = object : Migration(124, 125) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create NEONATAL_OUTCOME table for tracking detailed newborn health information
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS NEONATAL_OUTCOME (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        deliveryOutcomeId INTEGER NOT NULL,
                        neonateIndex INTEGER NOT NULL,
                        neonateUniqueId TEXT,
                        outcomeAtBirth TEXT,
                        outcomeAtBirthId INTEGER,
                        sex TEXT,
                        sexId INTEGER,
                        criedImmediately TEXT,
                        criedImmediatelyId INTEGER,
                        typeOfResuscitation TEXT,
                        birthWeight INTEGER,
                        congenitalAnomalyDetected TEXT,
                        congenitalAnomalyDetectedId INTEGER,
                        typeOfCongenitalAnomaly TEXT,
                        otherCongenitalAnomaly TEXT,
                        newbornComplications TEXT,
                        currentStatusOfBaby TEXT,
                        currentStatusOfBabyId INTEGER,
                        causeOfDeath TEXT,
                        otherCauseOfDeath TEXT,
                        birthDoseVaccinesGiven TEXT,
                        reasonForNoVaccines TEXT,
                        vitaminKInjectionGiven INTEGER,
                        reasonForNoVitaminK TEXT,
                        birthCertificateIssued TEXT,
                        birthCertificateIssuedId INTEGER,
                        isStillbirth INTEGER,
                        isNeonatalDeath INTEGER,
                        processed TEXT,
                        createdBy TEXT NOT NULL,
                        createdDate INTEGER NOT NULL,
                        updatedBy TEXT NOT NULL,
                        updatedDate INTEGER NOT NULL,
                        syncState INTEGER NOT NULL,
                        FOREIGN KEY(deliveryOutcomeId) REFERENCES DELIVERY_OUTCOME(id) ON UPDATE CASCADE ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS neonatalOutcomeInd ON NEONATAL_OUTCOME(deliveryOutcomeId)")
            }
        }

        val MIGRATION_125_126 = object : Migration(125, 126) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add BRD Neonatal Outcome fields to INFANT_REG table
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN outcomeAtBirth TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN typeOfResuscitation TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN newbornComplications TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN currentStatusOfBaby TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN causeOfDeath TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN otherCauseOfDeath TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN birthDoseVaccinesGiven TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN reasonForNoVaccines TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN vitaminKInjectionGiven INTEGER")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN reasonForNoVitaminK TEXT")
                db.execSQL("ALTER TABLE INFANT_REG ADD COLUMN birthCertificateIssued TEXT")
            }
        }

        val MIGRATION_126_127 = object : Migration(126, 127) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `OPHTHALMIC_VISIT` (" +
                            "`visitId` TEXT NOT NULL, " +
                            "`patientID` TEXT NOT NULL, " +
                            "`benVisitNo` INTEGER NOT NULL, " +
                            "`isDiabetic` INTEGER, " +
                            "`screeningPerformed` INTEGER, " +
                            "`visualAcuityChartUsed` TEXT, " +
                            "`distVARight` TEXT, " +
                            "`distVALeft` TEXT, " +
                            "`nearVA` TEXT, " +
                            "`caseIdConditions` TEXT, " +
                            "`cataractSymptoms` INTEGER, " +
                            "`glaucomaSymptoms` INTEGER, " +
                            "`diabeticRetinopathySymptoms` INTEGER, " +
                            "`presbyopiaSymptoms` INTEGER, " +
                            "`trachomaStatus` TEXT, " +
                            "`cornealDiseaseType` TEXT, " +
                            "`vitaminADeficiency` INTEGER, " +
                            "`injuryType` TEXT, " +
                            "`foreignBodyRemoval` TEXT, " +
                            "`chemicalExposure` INTEGER, " +
                            "`createdBy` TEXT NOT NULL, " +
                            "`createdDate` INTEGER NOT NULL, " +
                            "`updatedBy` TEXT NOT NULL, " +
                            "`updatedDate` INTEGER NOT NULL, " +
                            "`syncState` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`visitId`), " +
                            "FOREIGN KEY(`patientID`) REFERENCES `PATIENT`(`patientID`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ophthalmic_visit_patientID` ON `OPHTHALMIC_VISIT` (`patientID`)")
            }
        }
        val MIGRATION_127_128 = object : Migration(127, 128) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS EAR_DIAGNOSIS_ASSESSMENT (
                assessment_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                patient_id TEXT NOT NULL,
                ben_visit_no INTEGER,
                difficulty_hearing INTEGER,
                whisper_test_response TEXT,
                hearing_test_outcome TEXT,
                ear_pain INTEGER,
                ear_discharge_present INTEGER,
                foreign_body_in_ear TEXT,
                ear_condition_type TEXT,
                congenital_ear_malformation INTEGER
            )
        """.trimIndent()
                )
            }
        }
        val MIGRATION_128_129 = object : Migration(128, 129) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS PAIN_SYMPTOM_ASSESSMENT (
                assessment_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                patient_id TEXT NOT NULL,
                ben_visit_no INTEGER,
                pain_severity TEXT,
                pain_duration TEXT,
                symptoms_present INTEGER,
                other_symptoms_severity TEXT,
                immediate_relief_provided INTEGER
            )
            """.trimIndent()
                )
            }
        }
        val MIGRATION_129_130 = object : Migration(129, 130) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS PSYCHOSOCIAL_CAREGIVER_SUPPORT (
                assessment_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                patient_id TEXT NOT NULL,
                ben_visit_no INTEGER,
                psychosocial_counselling_provided INTEGER,
                caregiver_counselling_provided INTEGER,
                caregiver_distress_identified INTEGER,
                counselling_remarks TEXT
            )
            """.trimIndent()
                )
            }
        }
        val MIGRATION_130_131 = object : Migration(130, 131) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS ORAL_HEALTH (
                oral_health_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                patient_id TEXT NOT NULL,
                ben_visit_no INTEGER,
                tooth_decay_present INTEGER,
                tooth_decay_symptoms TEXT,
                gum_disease_present INTEGER,
                gum_disease_symptoms TEXT,
                irregular_teeth_jaws INTEGER,
                abnormal_growth_ulcer INTEGER,
                cleft_lip_palate INTEGER,
                dental_fluorosis INTEGER,
                dental_emergency TEXT,
                created_date INTEGER,
                created_by TEXT
            )
            """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_oral_health_patient_id " +
                        "ON ORAL_HEALTH(patient_id)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_oral_health_patient_visit " +
                        "ON ORAL_HEALTH(patient_id, ben_visit_no)"
                )
            }
        }

        val MIGRATION_131_132 = object : Migration(131, 132) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE PAIN_SYMPTOM_ASSESSMENT ADD COLUMN referral_required INTEGER")
                database.execSQL("ALTER TABLE PAIN_SYMPTOM_ASSESSMENT ADD COLUMN referral_level TEXT")
                database.execSQL("ALTER TABLE PAIN_SYMPTOM_ASSESSMENT ADD COLUMN reason_for_referral TEXT")
                database.execSQL("ALTER TABLE PAIN_SYMPTOM_ASSESSMENT ADD COLUMN follow_up_required INTEGER")
                database.execSQL("ALTER TABLE PAIN_SYMPTOM_ASSESSMENT ADD COLUMN follow_up_date TEXT")

                // Elderly Health – Section F: Referral & Follow-up
                database.execSQL("ALTER TABLE PSYCHOSOCIAL_CAREGIVER_SUPPORT ADD COLUMN referral_required INTEGER")
                database.execSQL("ALTER TABLE PSYCHOSOCIAL_CAREGIVER_SUPPORT ADD COLUMN referral_level TEXT")
                database.execSQL("ALTER TABLE PSYCHOSOCIAL_CAREGIVER_SUPPORT ADD COLUMN reason_for_referral TEXT")
                database.execSQL("ALTER TABLE PSYCHOSOCIAL_CAREGIVER_SUPPORT ADD COLUMN follow_up_required INTEGER")
                database.execSQL("ALTER TABLE PSYCHOSOCIAL_CAREGIVER_SUPPORT ADD COLUMN follow_up_date TEXT")
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
                            MIGRATION_111_112,
                            MIGRATION_112_113,
                            MIGRATION_113_114,
                            MIGRATION_114_115,
                            MIGRATION_115_116,
                            MIGRATION_116_117,
                            MIGRATION_117_118,
                            MIGRATION_118_119,
                            MIGRATION_119_120,
                            MIGRATION_120_121,
                            MIGRATION_121_122,
                            MIGRATION_122_123,
                            MIGRATION_123_124,
                            MIGRATION_124_125,
                            MIGRATION_125_126,
                            MIGRATION_126_127,
                            MIGRATION_127_128,
                            MIGRATION_128_129,
                            MIGRATION_129_130,
                            MIGRATION_130_131,
                            MIGRATION_131_132
                        )
                        .fallbackToDestructiveMigration()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                // Populate STATUS_OF_WOMAN_MASTER on fresh database creation
                                db.execSQL("INSERT OR IGNORE INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (1, 'Eligible Couple', 'EC')")
                                db.execSQL("INSERT OR IGNORE INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (2, 'Pregnant Woman', 'PW')")
                                db.execSQL("INSERT OR IGNORE INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (3, 'Postnatal', 'PN')")
                                db.execSQL("INSERT OR IGNORE INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (4, 'Elderly', 'EL')")
                                db.execSQL("INSERT OR IGNORE INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (5, 'Adolescent', 'AD')")
                                db.execSQL("INSERT OR IGNORE INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (6, 'Permanent Sterilization', 'ST')")
                                db.execSQL("INSERT OR IGNORE INTO STATUS_OF_WOMAN_MASTER (statusID, statusName, statusCode) VALUES (7, 'Not Applicable', 'NA')")
                            }
                        })
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