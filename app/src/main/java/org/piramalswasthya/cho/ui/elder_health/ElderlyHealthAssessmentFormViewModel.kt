package org.piramalswasthya.cho.ui.elder_health
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.ElderlyHealthRepo
import org.piramalswasthya.cho.model.ElderlyHealthAssessment
import org.piramalswasthya.cho.configuration.ElderlyHealthAssessmentDataset
import timber.log.Timber
import javax.inject.Inject
import org.piramalswasthya.cho.ui.commons.BaseFormViewModel

@HiltViewModel
class ElderlyHealthAssessmentFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val patientRepo: PatientRepo,
    private val userRepo: UserRepo,
    private val elderlyHealthRepo: ElderlyHealthRepo
) : BaseFormViewModel() {

    /* -------------------- BEN DETAILS -------------------- */

    val patientID: String? = savedStateHandle["patientID"]
    val benVisitNo: Int? = savedStateHandle["benVisitNo"]

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val dataset =
        ElderlyHealthAssessmentDataset(context, preferenceDao.getCurrentLanguage())

    val formList = dataset.listFlow

    private lateinit var assessmentCache: ElderlyHealthAssessment


    init {
        viewModelScope.launch {
            try {
                val user = userRepo.getLoggedInUser()
                if (user == null) {
                    Timber.e("No logged in user found")
                    _error.postValue("No logged in user found")
                    return@launch
                }

                val patient = patientID?.let { patientRepo.getPatientDisplay(it) }
                if (patient == null) {
                    Timber.e("Patient not found for ID: $patientID")
                    _error.postValue("Patient not found")
                    return@launch
                }

                _benName.value =
                    "${patient.patient.firstName} ${patient.patient.lastName ?: ""}"

                _benAgeGender.value =
                    "${patient.patient.age} ${patient.ageUnit?.name ?: ""} | ${patient.gender?.genderName ?: ""}"

                val existingRecord = benVisitNo?.let {
                    elderlyHealthRepo.getAssessment(patient.patient.patientID, it)
                }

                if (existingRecord != null) {
                    assessmentCache = existingRecord
                } else {
                    val latestRecord = elderlyHealthRepo.getAssessmentByPatientId(patient.patient.patientID)
                    if (latestRecord != null) {
                        assessmentCache = latestRecord.copy(
                            assessmentId = 0L,
                            benVisitNo = benVisitNo ?: 0
                        )
                    } else {
                        assessmentCache = ElderlyHealthAssessment(
                            patientID = patient.patient.patientID,
                            benVisitNo = benVisitNo ?: 0,
                        geriatricComplaints = null,
                        multipleChronicConditions = null,
                        recentFalls = null,
                        difficultyWalkingBalance = null,
                        visualHearingDifficulty = null,
                        functionalDecline = null,
                        memoryLoss = null
                    )
                }
                }

                setupDatasetCallbacks()

                dataset.setUpPage(
                    savedRecord = assessmentCache,
                    patientAge = patient.patient.age
                )

            } catch (e: Exception) {
                Timber.e(e, "Error initializing ElderlyHealthAssessmentFormViewModel")
            }
        }
    }


    private fun setupDatasetCallbacks() {
        dataset.onShowAlert = { message ->
            Timber.d("Dataset requested alert: $message")
            _showAlert.postValue(message)
        }
    }


    fun updateListOnValueChanged(formId: Int, index: Int) {
        Timber.d("updateListOnValueChanged: formId=$formId, index=$index")
        launchUpdateList(dataset, formId, index, "Error updating elderly assessment form")
    }


    fun saveForm() {
        launchSave("Saving Elderly Health Assessment failed") {
            check(this@ElderlyHealthAssessmentFormViewModel::assessmentCache.isInitialized) {
                "Assessment cache not initialized"
            }

            dataset.mapValues(assessmentCache, 1)

            elderlyHealthRepo.saveAssessment(assessmentCache)

            Timber.d("Elderly Health Assessment saved")
        }
    }
}