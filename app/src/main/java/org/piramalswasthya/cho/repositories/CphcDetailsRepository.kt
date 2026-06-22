package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.room.dao.EarDiagnosisAssessmentDao
import org.piramalswasthya.cho.database.room.dao.ElderlyHealthAssessmentDao
import org.piramalswasthya.cho.database.room.dao.MentalHealthScreeningDao
import org.piramalswasthya.cho.database.room.dao.NoseDiagnosisAssessmentDao
import org.piramalswasthya.cho.database.room.dao.OralHealthDao
import org.piramalswasthya.cho.database.room.dao.OphthalmicDao
import org.piramalswasthya.cho.database.room.dao.PainAndSymptomAssessmentDao
import org.piramalswasthya.cho.database.room.dao.PsychosocialCaregiverSupportDao
import org.piramalswasthya.cho.database.room.dao.ThroatDiagnosisAssessmentDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.VisitDB
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CphcDetailsRepository @Inject constructor(
    private val visitReasonsAndCategoriesDao: VisitReasonsAndCategoriesDao,
    private val earDiagnosisAssessmentDao: EarDiagnosisAssessmentDao,
    private val noseDiagnosisAssessmentDao: NoseDiagnosisAssessmentDao,
    private val throatDiagnosisAssessmentDao: ThroatDiagnosisAssessmentDao,
    private val oralHealthDao: OralHealthDao,
    private val ophthalmicDao: OphthalmicDao,
    private val elderlyHealthAssessmentDao: ElderlyHealthAssessmentDao,
    private val mentalHealthScreeningDao: MentalHealthScreeningDao,
    private val painAndSymptomAssessmentDao: PainAndSymptomAssessmentDao,
    private val psychosocialCaregiverSupportDao: PsychosocialCaregiverSupportDao,
) {

    suspend fun getLatestVisitDb(patientID: String, benVisitNo: Int): VisitDB? {
        return withContext(Dispatchers.IO) {
            visitReasonsAndCategoriesDao.getLatestVisitDbByPatientIDAndBenVisitNo(patientID, benVisitNo)
        }
    }

    suspend fun replaceVisitAndChiefComplaints(
        visitDB: VisitDB,
        chiefComplaints: List<ChiefComplaintDB>,
        patientID: String,
        benVisitNo: Int,
    ) {
        withContext(Dispatchers.IO) {
            visitReasonsAndCategoriesDao.deleteVisitDbByPatientIdAndBenVisitNo(patientID, benVisitNo)
            visitReasonsAndCategoriesDao.deleteChiefComplaintsByPatientIdAndBenVisitNo(patientID, benVisitNo)
            visitReasonsAndCategoriesDao.insertVisitDB(visitDB)
            if (chiefComplaints.isNotEmpty()) {
                visitReasonsAndCategoriesDao.insertAll(chiefComplaints)
            }
        }
    }

    suspend fun replaceVisitDb(visitDB: VisitDB, patientID: String, benVisitNo: Int) {
        withContext(Dispatchers.IO) {
            visitReasonsAndCategoriesDao.deleteVisitDbByPatientIdAndBenVisitNo(patientID, benVisitNo)
            visitReasonsAndCategoriesDao.insertVisitDB(visitDB)
        }
    }

    suspend fun clearAssessmentsForVisit(patientID: String, benVisitNo: Int) {
        withContext(Dispatchers.IO) {
            earDiagnosisAssessmentDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            noseDiagnosisAssessmentDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            throatDiagnosisAssessmentDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            oralHealthDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            ophthalmicDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            elderlyHealthAssessmentDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            mentalHealthScreeningDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            painAndSymptomAssessmentDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
            psychosocialCaregiverSupportDao.deleteByPatientIdAndVisitNo(patientID, benVisitNo)
        }
    }
}
