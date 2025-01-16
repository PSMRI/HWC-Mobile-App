package org.piramalswasthya.cho.di


import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.BatchDao
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
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
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.AbhaApiService
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.network.FlwApiService
import org.piramalswasthya.cho.network.interceptors.ContentTypeInterceptor
import org.piramalswasthya.cho.network.interceptors.TokenESanjeevaniInterceptor
import org.piramalswasthya.cho.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.utils.KeyUtils
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val baseClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(ContentTypeInterceptor())
            .build()

    @Singleton
    @Provides
    fun provideMoshiInstance(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }



    @Singleton
    @Provides
    @Named("uatClient")
    fun provideTmcHttpClient(): OkHttpClient {
        return baseClient
            .newBuilder()
            .connectTimeout(600, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS)
            .writeTimeout(600, TimeUnit.SECONDS)
            .addInterceptor(TokenInsertTmcInterceptor())
            .build()
    }
    @Singleton
    @Provides
    @Named("eSanjeevaniClient")
    fun provideESanjeevaniHttpClient(): OkHttpClient {
        return baseClient
            .newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(TokenESanjeevaniInterceptor())
            .build()
    }
//
    @Singleton
    @Provides
    @Named("abhaClient")
    fun provideAbhaHttpClient(): OkHttpClient {
        return baseClient
            .newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(TokenInsertAbhaInterceptor())
            .build()
    }


@Singleton
@Provides
fun provideESanjeevaniApiService(
    moshi: Moshi,
    @Named("eSanjeevaniClient") httpClient: OkHttpClient
): ESanjeevaniApiService {
    return Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(KeyUtils.sanjeevaniApiUrl())
        .client(httpClient)
        .build()
        .create(ESanjeevaniApiService::class.java)
}

    @Singleton
    @Provides
    fun provideAmritApiService(
        moshi: Moshi,
        @Named("uatClient") httpClient: OkHttpClient
    ): AmritApiService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(KeyUtils.baseAmritUrl())
            .client(httpClient)
            .build()
            .create(AmritApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideFlwApiService(
        moshi: Moshi,
        @Named("uatClient") httpClient: OkHttpClient
    ): FlwApiService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(KeyUtils.baseFlwUrl())
            .client(httpClient)
            .build()
            .create(FlwApiService::class.java)
    }
//
    @Singleton
    @Provides
    fun provideAbhaApiService(
        moshi: Moshi,
        @Named("abhaClient") httpClient: OkHttpClient
    ): AbhaApiService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.addConverterFactory(GsonConverterFactory.create())
            .baseUrl(KeyUtils.baseAbhaUrl())
            .client(httpClient)
            .build()
            .create(AbhaApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context) = InAppDb.getInstance(context)

    @Singleton
    @Provides
    fun provideUserDao(database : InAppDb) : UserDao = database.userDao

    @Singleton
    @Provides
    fun provideUserAuthDao(database : InAppDb) : UserAuthDao = database.userAuthDao

    @Singleton
    @Provides
    fun provideLoginSettingsDataDao(database : InAppDb) : LoginSettingsDataDao = database.loginSettingsDataDao

    @Singleton
    @Provides
    fun provideLanguageDao(database : InAppDb) : LanguageDao = database.languageDao

    @Singleton
    @Provides
    fun provideVisitReasonsAndCategoriesDao(database : InAppDb) : VisitReasonsAndCategoriesDao = database.visitReasonsAndCategoriesDao

    @Singleton
    @Provides
    fun provideRegistrarMasterDataDao(database : InAppDb) : RegistrarMasterDataDao = database.registrarMasterDataDao

    @Singleton
    @Provides
    fun provideStateMasterDao(database : InAppDb) : StateMasterDao = database.stateMasterDao

    @Singleton
    @Provides
    fun provideVaccinationTypeAndDoseDao(database : InAppDb) : VaccinationTypeAndDoseDao = database.vaccinationTypeAndDoseDao

    @Singleton
    @Provides
    fun provideDistrictMasterDao(database : InAppDb) : DistrictMasterDao = database.districtMasterDao

    @Singleton
    @Provides
    fun provideBlockMasterDao(database : InAppDb) : BlockMasterDao = database.blockMasterDao

    @Singleton
    @Provides
    fun provideVillageMasterDao(database : InAppDb) : VillageMasterDao = database.villageMasterDao

    @Singleton
    @Provides
    fun provideGovIdEntityMasterDao(database : InAppDb) : GovIdEntityMasterDao = database.govIdEntityMasterDao

    @Singleton
    @Provides
    fun provideOtherGovIdEntityMasterDao(database : InAppDb) : OtherGovIdEntityMasterDao = database.otherGovIdEntityMasterDao

    @Singleton
    @Provides
    fun provideChiefComplaintEntityMasterDao(database: InAppDb): ChiefComplaintMasterDao = database.chiefComplaintMasterDao

    @Singleton
    @Provides
    fun provideSubVisitEntityCat(database: InAppDb): SubCatVisitDao = database.subCatVisitDao

    @Singleton
    @Provides
    fun provideIllnessDropdownEntityDao(database: InAppDb): HistoryDao = database.historyDao

    @Singleton
    @Provides
    fun provideVitalsDao(database: InAppDb): VitalsDao = database.vitalsDao

    @Singleton
    @Provides
    fun provideProcedureDao(database: InAppDb): ProcedureDao = database.procedureDao

    @Singleton
    @Provides
    fun providePrescriptionTemplateDao(database: InAppDb): PrescriptionTemplateDao = database.prescriptionTemplateDao

    @Singleton
    @Provides
    fun provideReferRevisitDao(database: InAppDb): ReferRevisitDao = database.referRevisitDao

    @Singleton
    @Provides
    fun provideHigherHealthCenterDao(database: InAppDb): HealthCenterDao = database.healthCenterDao

    @Singleton
    @Provides
    fun providePatientDao(database: InAppDb): PatientDao = database.patientDao

    @Singleton
    @Provides
    fun provideCasesReaderDao(database: InAppDb): CaseRecordeDao = database.caseRecordeDao

    @Singleton
    @Provides
    fun provideBenFlowDao(database: InAppDb): BenFlowDao = database.benFlowDao

    @Singleton
    @Provides
    fun providePatientVisitInfoSyncDao(database: InAppDb): PatientVisitInfoSyncDao = database.patientVisitInfoSyncDao

    @Singleton
    @Provides
    fun provideMaternalHealthDao(database: InAppDb): MaternalHealthDao = database.maternalHealthDao

    @Singleton
    @Provides
    fun provideImmunizationDao(database: InAppDb): ImmunizationDao = database.immunizationDao

    @Singleton
    @Provides
    fun provideDeliveryOutcomeDao(database: InAppDb): DeliveryOutcomeDao = database.deliveryOutcomeDao

    @Singleton
    @Provides
    fun providePncDao(database: InAppDb): PncDao = database.pncDao

    @Singleton
    @Provides
    fun provideEcrDao(database: InAppDb): EcrDao = database.ecrDao

    @Singleton
    @Provides
    fun providePreferenceDao(@ApplicationContext context: Context) = PreferenceDao(context)


    @Singleton
    @Provides
    fun provideInvestigationDao(database: InAppDb): InvestigationDao = database.investigationDao

    @Singleton
    @Provides
    fun provideOutreachDao(database: InAppDb): OutreachDao = database.outreachDao

    @Singleton
    @Provides
    fun providePrescriptionDao(database: InAppDb): PrescriptionDao = database.prescriptionDao

    @Singleton
    @Provides
    fun provideProcedureMasterDao(database: InAppDb): ProcedureMasterDao = database.procedureMasterDao
    @Singleton
    @Provides
    fun provideBatchDao(database: InAppDb): BatchDao = database.batchDao
}