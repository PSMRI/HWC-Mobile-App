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
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.converters.LocationEntityListConverter
import org.piramalswasthya.cho.database.converters.LoginSettingsDataConverter
import org.piramalswasthya.cho.database.converters.StateConverter
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
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
import org.piramalswasthya.cho.database.room.dao.LoginSettingsDataDao
import org.piramalswasthya.cho.database.room.dao.LanguageDao
import org.piramalswasthya.cho.database.room.dao.MaternalHealthDao
import org.piramalswasthya.cho.database.room.dao.OtherGovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.OutreachDao
import org.piramalswasthya.cho.database.room.dao.ReferRevisitDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.room.dao.PncDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionTemplateDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.ProcedureMasterDao
import org.piramalswasthya.cho.database.room.dao.RegistrarMasterDataDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.SubCatVisitDao
import org.piramalswasthya.cho.database.room.dao.UserAuthDao
import org.piramalswasthya.cho.database.room.dao.VaccinationTypeAndDoseDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.moddel.OccupationMaster
import org.piramalswasthya.cho.model.*
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import timber.log.Timber

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
        FingerPrint::class,
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
    version = 109, exportSchema = false
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

//        val MIGRATION_107_108 = object : Migration(107, 108) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("""
//            CREATE TABLE IF NOT EXISTS CBAC_new (
//                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                patId TEXT NOT NULL,
//                fillDate INTEGER NOT NULL,
//                cbac_age_posi INTEGER NOT NULL,
//                cbac_smoke_posi INTEGER NOT NULL,
//                cbac_alcohol_posi INTEGER NOT NULL,
//                cbac_waist_posi INTEGER NOT NULL,
//                cbac_pa_posi INTEGER NOT NULL,
//                cbac_familyhistory_posi INTEGER NOT NULL,
//                total_score INTEGER NOT NULL,
//                cbac_sufferingtb_pos INTEGER NOT NULL,
//                cbac_antitbdrugs_pos INTEGER NOT NULL,
//                cbac_tbhistory_pos INTEGER NOT NULL,
//                cbac_sortnesofbirth_pos INTEGER NOT NULL,
//                cbac_coughing_pos INTEGER NOT NULL,
//                cbac_bloodsputum_pos INTEGER NOT NULL,
//                cbac_fivermore_pos INTEGER NOT NULL,
//                cbac_loseofweight_pos INTEGER NOT NULL,
//                cbac_nightsweats_pos INTEGER NOT NULL,
//                cbac_historyoffits_pos INTEGER NOT NULL,
//                cbac_difficultyinmouth_pos INTEGER NOT NULL,
//                cbac_uicers_pos INTEGER NOT NULL,
//                cbac_toneofvoice_pos INTEGER NOT NULL,
//                cbac_lumpinbreast_pos INTEGER NOT NULL,
//                cbac_blooddischage_pos INTEGER NOT NULL,
//                cbac_changeinbreast_pos INTEGER NOT NULL,
//                cbac_bleedingbtwnperiods_pos INTEGER NOT NULL,
//                cbac_bleedingaftermenopause_pos INTEGER NOT NULL,
//                cbac_bleedingafterintercourse_pos INTEGER NOT NULL,
//                cbac_foulveginaldischarge_pos INTEGER NOT NULL,
//                cbac_cloudy_posi INTEGER NOT NULL,
//                cbac_diffreading_posi INTEGER NOT NULL,
//                cbac_pain_ineyes_posi INTEGER NOT NULL,
//                cbac_diff_inhearing_posi INTEGER NOT NULL,
//                cbac_redness_ineyes_posi INTEGER NOT NULL,
//                cbac_growth_in_mouth_posi INTEGER NOT NULL,
//                cbac_white_or_red_patch_posi INTEGER NOT NULL,
//                cbac_Pain_while_chewing_posi INTEGER NOT NULL,
//                cbac_hyper_pigmented_patch_posi INTEGER NOT NULL,
//                cbac_any_thickend_skin_posi INTEGER NOT NULL,
//                cbac_nodules_on_skin_posi INTEGER NOT NULL,
//                cbac_numbness_on_palm_posi INTEGER NOT NULL,
//                cbac_clawing_of_fingers_posi INTEGER NOT NULL,
//                cbac_tingling_palm_posi INTEGER NOT NULL,
//                cbac_tingling_or_numbness_posi INTEGER NOT NULL,
//                cbac_inability_close_eyelid_posi INTEGER NOT NULL,
//                cbac_diff_holding_obj_posi INTEGER NOT NULL,
//                cbac_weekness_in_feet_posi INTEGER NOT NULL,
//                cbac_fuel_used_posi INTEGER NOT NULL,
//                cbac_occupational_exposure_posi INTEGER NOT NULL,
//                cbac_feeling_unsteady_posi INTEGER NOT NULL,
//                cbac_suffer_physical_disability_posi INTEGER NOT NULL,
//                cbac_needing_help_posi INTEGER NOT NULL,
//                cbac_forgetting_names_posi INTEGER NOT NULL,
//                cbac_little_interest_posi INTEGER NOT NULL,
//                cbac_feeling_down_posi INTEGER NOT NULL,
//                cbac_little_interest_score INTEGER NOT NULL,
//                cbac_feeling_down_score INTEGER NOT NULL,
//                cbac_referpatient_mo TEXT,
//                cbac_tracing_all_fm TEXT,
//                cbac_sputemcollection TEXT,
//                serverUpdatedStatus INTEGER NOT NULL,
//                createdBy TEXT,
//                createdDate INTEGER NOT NULL,
//                ProviderServiceMapID INTEGER NOT NULL,
//                VanID INTEGER NOT NULL,
//                Processed TEXT,
//                Countyid INTEGER NOT NULL,
//                stateid INTEGER NOT NULL,
//                districtid INTEGER NOT NULL,
//                districtname TEXT,
//                villageid INTEGER NOT NULL,
//                cbac_reg_id INTEGER NOT NULL,
//                hrp_suspected INTEGER,
//                suspected_hrp TEXT,
//                confirmed_hrp TEXT,
//                ncd_suspected TEXT,
//                suspected_ncd TEXT,
//                ncd_confirmed INTEGER,
//                confirmed_ncd TEXT,
//                suspected_tb TEXT,
//                confirmed_tb TEXT,
//                suspected_ncd_diseases TEXT,
//                confirmed_ncd_diseases TEXT,
//                diagnosis_status TEXT,
//                syncState INTEGER NOT NULL DEFAULT 0,
//                FOREIGN KEY(patId) REFERENCES PATIENT(patientID) ON UPDATE CASCADE ON DELETE CASCADE
//            )
//        """.trimIndent())
//
//
//
//                try {
//                    database.execSQL("""
//            INSERT INTO CBAC_new (
//                id, patId, fillDate, cbac_age_posi, cbac_smoke_posi, cbac_alcohol_posi, cbac_waist_posi,
//                cbac_pa_posi, cbac_familyhistory_posi, total_score, cbac_sufferingtb_pos, cbac_antitbdrugs_pos,
//                cbac_tbhistory_pos, cbac_sortnesofbirth_pos, cbac_coughing_pos, cbac_bloodsputum_pos, cbac_fivermore_pos,
//                cbac_loseofweight_pos, cbac_nightsweats_pos, cbac_historyoffits_pos, cbac_difficultyinmouth_pos,
//                cbac_uicers_pos, cbac_toneofvoice_pos, cbac_lumpinbreast_pos, cbac_blooddischage_pos, cbac_changeinbreast_pos,
//                cbac_bleedingbtwnperiods_pos, cbac_bleedingaftermenopause_pos, cbac_bleedingafterintercourse_pos,
//                cbac_foulveginaldischarge_pos, cbac_cloudy_posi, cbac_diffreading_posi, cbac_pain_ineyes_posi,
//                cbac_diff_inhearing_posi, cbac_redness_ineyes_posi, cbac_growth_in_mouth_posi, cbac_white_or_red_patch_posi,
//                cbac_Pain_while_chewing_posi, cbac_hyper_pigmented_patch_posi, cbac_any_thickend_skin_posi,
//                cbac_nodules_on_skin_posi, cbac_numbness_on_palm_posi, cbac_clawing_of_fingers_posi, cbac_tingling_palm_posi,
//                cbac_tingling_or_numbness_posi, cbac_inability_close_eyelid_posi, cbac_diff_holding_obj_posi,
//                cbac_weekness_in_feet_posi, cbac_fuel_used_posi, cbac_occupational_exposure_posi, cbac_feeling_unsteady_posi,
//                cbac_suffer_physical_disability_posi, cbac_needing_help_posi, cbac_forgetting_names_posi,
//                cbac_little_interest_posi, cbac_feeling_down_posi, cbac_little_interest_score, cbac_feeling_down_score,
//                cbac_referpatient_mo, cbac_tracing_all_fm, cbac_sputemcollection, serverUpdatedStatus, createdBy,
//                createdDate, ProviderServiceMapID, VanID, Processed, Countyid, stateid, districtid, districtname, villageid,
//                cbac_reg_id, hrp_suspected, suspected_hrp, confirmed_hrp, ncd_suspected, suspected_ncd, ncd_confirmed,
//                confirmed_ncd, suspected_tb, confirmed_tb, suspected_ncd_diseases, confirmed_ncd_diseases, diagnosis_status,
//                syncState
//            )
//            SELECT
//                id, patId, fillDate, cbac_age_posi, cbac_smoke_posi, cbac_alcohol_posi, cbac_waist_posi,
//                cbac_pa_posi, cbac_familyhistory_posi, total_score, cbac_sufferingtb_pos, cbac_antitbdrugs_pos,
//                cbac_tbhistory_pos, cbac_sortnesofbirth_pos, cbac_coughing_pos, cbac_bloodsputum_pos, cbac_fivermore_pos,
//                cbac_loseofweight_pos, cbac_nightsweats_pos, cbac_historyoffits_pos, cbac_difficultyinmouth_pos,
//                cbac_uicers_pos, cbac_toneofvoice_pos, cbac_lumpinbreast_pos, cbac_blooddischage_pos, cbac_changeinbreast_pos,
//                cbac_bleedingbtwnperiods_pos, cbac_bleedingaftermenopause_pos, cbac_bleedingafterintercourse_pos,
//                cbac_foulveginaldischarge_pos, cbac_cloudy_posi, cbac_diffreading_posi, cbac_pain_ineyes_posi,
//                cbac_diff_inhearing_posi, cbac_redness_ineyes_posi, cbac_growth_in_mouth_posi, cbac_white_or_red_patch_posi,
//                cbac_Pain_while_chewing_posi, cbac_hyper_pigmented_patch_posi, cbac_any_thickend_skin_posi,
//                cbac_nodules_on_skin_posi, cbac_numbness_on_palm_posi, cbac_clawing_of_fingers_posi, cbac_tingling_palm_posi,
//                cbac_tingling_or_numbness_posi, cbac_inability_close_eyelid_posi, cbac_diff_holding_obj_posi,
//                cbac_weekness_in_feet_posi, cbac_fuel_used_posi, cbac_occupational_exposure_posi, cbac_feeling_unsteady_posi,
//                cbac_suffer_physical_disability_posi, cbac_needing_help_posi, cbac_forgetting_names_posi,
//                cbac_little_interest_posi, cbac_feeling_down_posi, cbac_little_interest_score, cbac_feeling_down_score,
//                cbac_referpatient_mo, cbac_tracing_all_fm, cbac_sputemcollection, serverUpdatedStatus, createdBy,
//                createdDate, ProviderServiceMapID, VanID, Processed, Countyid, stateid, districtid, districtname, villageid,
//                cbac_reg_id, hrp_suspected, suspected_hrp, confirmed_hrp, ncd_suspected, suspected_ncd, ncd_confirmed,
//                confirmed_ncd, suspected_tb, confirmed_tb, suspected_ncd_diseases, confirmed_ncd_diseases, diagnosis_status,
//                syncState
//            FROM CBAC
//        """.trimIndent())
//                    database.execSQL("DROP TABLE CBAC")
//                    database.execSQL("ALTER TABLE CBAC_new RENAME TO CBAC")
//                } catch (e:Exception) {
//                    Timber.e(e, "Migration 1 -> 2 failed")
//                }
//
//            }
//        }
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

                        .addMigrations(MIGRATION_106_107, MIGRATION_107_108,MIGRATION_108_109)
                        .fallbackToDestructiveMigration()
                        .setQueryCallback(
                            object : QueryCallback {
                                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
//                                    Timber.d("Query to Room : sqlQuery=$sqlQuery with arguments : $bindArgs")
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