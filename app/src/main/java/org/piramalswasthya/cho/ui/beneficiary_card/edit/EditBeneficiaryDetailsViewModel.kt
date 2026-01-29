package org.piramalswasthya.cho.ui.beneficiary_card.edit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.StatusOfWomanDao
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.StatusOfWomanMaster
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.utils.DateTimeUtil
import javax.inject.Inject

@HiltViewModel
class EditBeneficiaryDetailsViewModel @Inject constructor(
    private val patientDao: PatientDao,
    private val patientRepo: PatientRepo,
    private val statusOfWomanDao: StatusOfWomanDao
) : ViewModel() {

    enum class SaveState {
        IDLE,
        SAVING,
        SUCCESS,
        ERROR
    }

    private val _saveState = MutableLiveData(SaveState.IDLE)
    val saveState: LiveData<SaveState>
        get() = _saveState

    private val _patientInfo = MutableLiveData<PatientDisplayWithVisitInfo?>()
    val patientInfo: LiveData<PatientDisplayWithVisitInfo?>
        get() = _patientInfo

    private val _statusOfWomanList = MutableLiveData<List<StatusOfWomanMaster>>()
    val statusOfWomanList: LiveData<List<StatusOfWomanMaster>>
        get() = _statusOfWomanList

    private val _filteredStatusOfWomanList = MutableLiveData<List<StatusOfWomanMaster>>()
    val filteredStatusOfWomanList: LiveData<List<StatusOfWomanMaster>>
        get() = _filteredStatusOfWomanList

    // Field values
    var lastName: String? = null
    var phoneNumber: String? = null
    var ageYears: Int? = null
    var ageMonths: Int? = null
    var ageDays: Int? = null
    var selectedStatusOfWoman: StatusOfWomanMaster? = null

   
    private var originalStatusOfWomanId: Int? = null

  
    private val _ageYearsValid = MutableLiveData(true)
    val ageYearsValid: LiveData<Boolean>
        get() = _ageYearsValid

    private val _phoneNumberValid = MutableLiveData(true)
    val phoneNumberValid: LiveData<Boolean>
        get() = _phoneNumberValid

    private val _statusOfWomanValid = MutableLiveData(true)
    val statusOfWomanValid: LiveData<Boolean>
        get() = _statusOfWomanValid

    // Status of Woman changed flag
    private val _statusOfWomanChanged = MutableLiveData(false)
    val statusOfWomanChanged: LiveData<Boolean>
        get() = _statusOfWomanChanged

    init {
        loadStatusOfWomanMaster()
    }

    private fun loadStatusOfWomanMaster() {
        viewModelScope.launch {
            try {
                val list = statusOfWomanDao.getAllStatusOfWoman()
                _statusOfWomanList.value = list
            } catch (e: Exception) {
                _statusOfWomanList.value = emptyList()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setPatientInfo(patient: PatientDisplayWithVisitInfo) {
        _patientInfo.value = patient

        
        lastName = patient.patient.lastName
        phoneNumber = patient.patient.phoneNo
        ageYears = patient.patient.age
        originalStatusOfWomanId = patient.patient.statusOfWomanID

       
        patient.patient.dob?.let { dob ->
            val age = DateTimeUtil.calculateAgePicker(dob)
            ageYears = age.years
            ageMonths = age.months
            ageDays = age.days
        }

      
        patient.patient.statusOfWomanID?.let { statusId ->
            viewModelScope.launch {
                selectedStatusOfWoman = statusOfWomanDao.getStatusById(statusId)
            }
        }

        
        updateFilteredStatusOfWomanList()
    }

    fun updateFilteredStatusOfWomanList() {
        val patient = _patientInfo.value?.patient ?: return
        val genderId = patient.genderID
        val maritalStatusId = patient.maritalStatusID

        // Only show for females (genderId = 2)
        if (genderId != 2) {
            _filteredStatusOfWomanList.value = emptyList()
            return
        }

        val allStatuses = _statusOfWomanList.value ?: return
        val currentAgeYears = ageYears ?: return

        val filtered = when {
            // Female, ≥50 → Elderly only
            currentAgeYears >= 50 ->
                allStatuses.filter { it.statusID == 4 }

            // Female, 10-19, Unmarried → Adolescent only
            currentAgeYears in 10..19 && maritalStatusId == 1 ->
                allStatuses.filter { it.statusID == 5 }

            // Female, ≥15, Married → EC, PW, Postnatal, Sterilization
            currentAgeYears >= 15 && maritalStatusId == 2 ->
                allStatuses.filter { it.statusID in listOf(1, 2, 3, 6) }

            // Female, 20-49, Unmarried → Not Applicable only
            currentAgeYears in 20..49 && maritalStatusId == 1 ->
                allStatuses.filter { it.statusID == 7 }

            else -> emptyList()
        }

        _filteredStatusOfWomanList.value = filtered
    }

    fun shouldShowStatusOfWoman(): Boolean {
        val patient = _patientInfo.value?.patient ?: return false
        return patient.genderID == 2 && (ageYears ?: 0) >= 10
    }

    fun validatePhoneNumber(phone: String?): Boolean {
        if (phone.isNullOrEmpty()) {
            _phoneNumberValid.value = true
            return true
        }

        val isValid = phone.length == 10 && phone[0] in listOf('6', '7', '8', '9')
        _phoneNumberValid.value = isValid
        return isValid
    }

    fun validateAgeYears(years: Int?): Boolean {
        val isValid = years != null && years >= 0 && years <= 120
        _ageYearsValid.value = isValid
        return isValid
    }

    fun validateStatusOfWoman(): Boolean {
        if (!shouldShowStatusOfWoman()) {
            _statusOfWomanValid.value = true
            return true
        }

        val isValid = selectedStatusOfWoman != null
        _statusOfWomanValid.value = isValid
        return isValid
    }

    fun validateAll(): Boolean {
        val phoneValid = validatePhoneNumber(phoneNumber)
        val ageValid = validateAgeYears(ageYears)
        val statusValid = validateStatusOfWoman()

        return phoneValid && ageValid && statusValid
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveChanges() {
        if (!validateAll()) {
            _saveState.value = SaveState.ERROR
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.SAVING

            try {
                val currentPatient = _patientInfo.value?.patient ?: throw Exception("Patient not found")

                // Update patient fields
                currentPatient.lastName = lastName
                currentPatient.phoneNo = phoneNumber

                // Update age and DOB
                val newDob = DateTimeUtil.calculateDateOfBirth(
                    ageYears ?: 0,
                    ageMonths ?: 0,
                    0, // weeks
                    ageDays ?: 0
                )
                currentPatient.dob = newDob
                currentPatient.age = ageYears

               
                val newStatusId = selectedStatusOfWoman?.statusID
                currentPatient.statusOfWomanID = newStatusId

                if (originalStatusOfWomanId != newStatusId) {
                    _statusOfWomanChanged.value = true
                }

                // Save to database
                patientDao.updatePatient(currentPatient)

                val updatedPatientInfo = _patientInfo.value?.copy(
                    patient = currentPatient
                )
                _patientInfo.value = updatedPatientInfo

                _saveState.value = SaveState.SUCCESS
            } catch (e: Exception) {
                _saveState.value = SaveState.ERROR
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.IDLE
    }

    fun getNewStatusOfWomanId(): Int? {
        return selectedStatusOfWoman?.statusID
    }
}
