package org.piramalswasthya.cho.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.piramalswasthya.cho.database.converters.DateConverter
import org.piramalswasthya.cho.database.converters.DistrictBlockConverter
import org.piramalswasthya.cho.database.converters.DistrictConverter
import org.piramalswasthya.cho.database.converters.LocationConverter
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.converters.LocationEntityListConverter
import org.piramalswasthya.cho.database.converters.LoginSettingsDataConverter
import org.piramalswasthya.cho.database.converters.StateConverter
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.converters.SyncStateConverter
import org.piramalswasthya.cho.database.converters.UserMasterLocationConverter
import org.piramalswasthya.cho.database.converters.VillageConverter
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
        CbacCache::class
    ],
    views = [PrescriptionWithItemMasterAndDrugFormMaster::class],
    version = 102, exportSchema = false
)


@TypeConverters(LocationEntityListConverter::class,
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
    UserMasterLocationConverter::class
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


    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null

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