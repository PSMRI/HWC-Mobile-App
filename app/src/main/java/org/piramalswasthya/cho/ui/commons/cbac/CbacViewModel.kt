package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.cbac

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
//import org.piramalswasthya.cho.database.room.dao.BenDao
//import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.AgeUnit
//import org.piramalswasthya.sakhi.model.BenBasicCache
//import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.cho.model.CbacCache
import org.piramalswasthya.cho.model.Gender
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.CbacRepo
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class CbacViewModel @Inject constructor(
    private val context: Application,
    state: SavedStateHandle,
    patientRepo: PatientRepo,
    preferenceDao: PreferenceDao,
    private val cbacRepo: CbacRepo
) : ViewModel() {

    enum class State {
        LOADING,
        IDLE,
        MISSING_FIELD,
        SAVING,
        SAVE_SUCCESS,
        SAVE_FAIL
    }


    private val englishResources by lazy {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.ENGLISH)
        context.createConfigurationContext(configuration).resources
    }

    private val resources by lazy {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale(preferenceDao.getCurrentLanguage().symbol))
        context.createConfigurationContext(configuration).resources
    }

    private val _raAgeScore = MutableLiveData(0)
    val raAgeScore = _raAgeScore.map { it.toString() }
    private val _raSmokeScore = MutableLiveData(0)
    val raSmokeScore = _raSmokeScore.map { it.toString() }
    private val _raAlcoholScore = MutableLiveData(0)
    val raAlcoholScore = _raAlcoholScore.map { it.toString() }
    private val _raWaistScore = MutableLiveData(0)
    val raWaistScore = _raWaistScore.map { it.toString() }
    private val _raPaScore = MutableLiveData(0)
    val raPaScore = _raPaScore.map { it.toString() }
    private val _raFhScore = MutableLiveData(0)
    val raFhScore = _raFhScore.map { it.toString() }
    private val _raTotalScore = MutableLiveData(0)
    val raTotalScore = _raTotalScore.map{
        String.format(
            "%s%s%s",
            resources.getString(R.string.total_score_wihout_semi_colon),
            ": ",
            it
        )
    }

    //PHQ2
    private val _phq2LittleInterestScore = MutableLiveData(0)
    val phq2LittleInterestScore = _phq2LittleInterestScore.map { it.toString() }
    private val _phq2FeelDownDepScore = MutableLiveData(0)
    val phq2FeelDownDepScore = _phq2FeelDownDepScore.map { it.toString() }
    private val _phq2TotalScore = MutableLiveData(0)
    val phq2TotalScore = _phq2TotalScore.map {
        String.format(
            "%s%s%s",
            resources.getString(R.string.total_score_wihout_semi_colon),
            ": ",
            it
        )
    }


    val raAgeText = _raAgeScore.map {
        val text = resources.getStringArray(R.array.cbac_age)[it]
        if (this::cbac.isInitialized)
            cbac.cbac_age_posi = it + 1
        text
    }

    private val patId = "";
    val cbacId = 0
    private val ashaId = 0

//    private val benId = CbacFragmentArgs.fromSavedStateHandle(state).benId
//    val cbacId = CbacFragmentArgs.fromSavedStateHandle(state).cbacId
//    private val ashaId = CbacFragmentArgs.fromSavedStateHandle(state).ashaId
    private lateinit var cbac: CbacCache
    private lateinit var ben: PatientDisplay

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender
    private val _gender = MutableLiveData<Gender>()
    val gender: LiveData<Gender>
        get() = _gender
    private val _age = MutableLiveData<Int>()
    val age: LiveData<Int>
        get() = _age
    private val _ast2 = MutableLiveData(0)
    val ast2: LiveData<Int>
        get() = _ast2
    private val _astMoic = MutableLiveData(0)
    val astMoic: LiveData<Int>
        get() = _astMoic
    private val _ast1 = MutableLiveData(0)
    val ast1: LiveData<Int>
        get() = _ast1


    private val _state = MutableLiveData(State.LOADING)
    val state: LiveData<State>
        get() = _state


    val _filledCbac = MutableLiveData<CbacCache>()
    val filledCbac: LiveData<CbacCache>
        get() = _filledCbac

    var missingFieldString: String? = null
    private var flagForPhq2 = false
    private var flagForNcd = false

    private val _minDate = MutableLiveData<Long>()


    val minDate: LiveData<Long>
        get() = _minDate

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cbac = if (cbacId > 0)
                    cbacRepo.getCbacCacheFromId(cbacId).also { _filledCbac.postValue(it) }
                else
                    CbacCache(
                        patId = patId, ashaId = ashaId,
                        syncState = SyncState.UNSYNCED,
                        createdDate = System.currentTimeMillis()
                    )
                val lastFilledCbac = cbacRepo.getLastFilledCbac(patId)
                ben = patientRepo.getPatientDisplay(patId)!!
                _minDate.postValue(lastFilledCbac?.fillDate?.let { it + TimeUnit.DAYS.toMillis(365) }
                    ?: ben.patient.registrationDate?.time ?: 0)
            }
            if (ben.ageUnit.name.lowercase() != "years")
                throw IllegalStateException("Age not in years for CBAC form!!")
            val age = ben.patient.age
//            val age = if (cbacId == 0) ben.age else BenBasicCache.getAgeFromDob(ben.dob)
            when (age) {
                in 0..29 -> {
                    _raAgeScore.value = 0
                }

                in 30..39 -> {
                    _raAgeScore.value = 1
                }

                in 40..49 -> {
                    _raAgeScore.value = 2
                }

                in 50..59 -> {
                    _raAgeScore.value = 3
                }

                else -> {
                    _raAgeScore.value = 4
                }
            }
            _raTotalScore.value =
                _raAgeScore.value!! + _raSmokeScore.value!! + _raAlcoholScore.value!! + _raWaistScore.value!! + _raPaScore.value!! + _raFhScore.value!!
            ben.gender?.let {
                if(it.genderName.lowercase() == "male"){
                    _gender.value = Gender.MALE
                }
                else if(it.genderName.lowercase() == "female"){
                    _gender.value = Gender.FEMALE
                }
                else{
                    _gender.value = Gender.TRANSGENDER
                }
            }
            _age.value = ben.patient.age ?: 0
            _benName.value = "${ben.patient.firstName} ${if (ben.patient.lastName == null) "" else ben.patient.lastName}"
            _benAgeGender.value = "${ben.patient.age} ${ben.ageUnit.name} | ${ben.gender.genderName}"

            _state.value = State.IDLE


        }
    }

    fun setSmoke(i: Int) {
        cbac.cbac_smoke_posi = i + 1
        //cbac.cbac_smoke = resources.getStringArray(R.array.cbac_smoke)[i]
        _raSmokeScore.value = when (i) {
            0 -> 0
            1 -> 1
            2 -> 2
            else -> 0
        }
        _raTotalScore.value =
            _raAgeScore.value!! + _raSmokeScore.value!! + _raAlcoholScore.value!! + _raWaistScore.value!! + _raPaScore.value!! + _raFhScore.value!!
        Timber.d("id : $i")
    }

    fun setAlcohol(i: Int) {
        cbac.cbac_alcohol_posi = i + 1
        _raAlcoholScore.value = when (i) {
            0 -> 0
            1 -> 1
            else -> 0
        }
        _raTotalScore.value =
            _raAgeScore.value!! + _raSmokeScore.value!! + _raAlcoholScore.value!! + _raWaistScore.value!! + _raPaScore.value!! + _raFhScore.value!!

        Timber.d("id : $i")
    }

    fun setWaist(i: Int) {
        cbac.cbac_waist_posi = i + 1
        _raWaistScore.value = when (i) {
            0 -> 0
            1 -> 1
            2 -> 2
            else -> 0
        }
        _raTotalScore.value =
            _raAgeScore.value!! + _raSmokeScore.value!! + _raAlcoholScore.value!! + _raWaistScore.value!! + _raPaScore.value!! + _raFhScore.value!!

        Timber.d("id : $i ")
    }

    fun setPa(i: Int) {
        cbac.cbac_pa_posi = i + 1
        _raPaScore.value = when (i) {
            0 -> 0
            1 -> 1
            else -> 0
        }
        _raTotalScore.value =
            _raAgeScore.value!! + _raSmokeScore.value!! + _raAlcoholScore.value!! + _raWaistScore.value!! + _raPaScore.value!! + _raFhScore.value!!

        Timber.d("id : $i val:")
    }

    fun setFh(i: Int) {
        cbac.cbac_familyhistory_posi = i + 1
        _raFhScore.value = when (i) {
            0 -> 0
            1 -> 2
            else -> 0
        }
        _raTotalScore.value =
            _raAgeScore.value!! + _raSmokeScore.value!! + _raAlcoholScore.value!! + _raWaistScore.value!! + _raPaScore.value!! + _raFhScore.value!!

        Timber.d("id : $i")
    }

    fun setFhTb(i: Int) {
        cbac.cbac_sufferingtb_pos = i
        if (i == 1) _ast2.value = _ast2.value?.plus(1)
        else if (i == 2 && ast2.value!! > 0) {
            _ast2.value = _ast2.value?.minus(1)
        }
    }

    fun setTakingTbDrug(i: Int) {
        cbac.cbac_antitbdrugs_pos = i
        if (i == 1) _ast2.value = _ast2.value?.plus(1)
        else if (i == 2 && ast2.value!! > 0) _ast2.value = _ast2.value?.minus(1)

    }

    fun setHisTb(i: Int) {
        cbac.cbac_tbhistory_pos = i
        if (i == 1) _ast1.value = _ast1.value?.plus(1)
        else if (i == 2 && ast1.value!! > 0) _ast1.value = _ast1.value?.minus(1)
    }

    fun setCoughing(i: Int) {
        cbac.cbac_coughing_pos = i
        if (i == 1) _ast1.value = _ast1.value?.plus(1)
        else if (i == 2 && ast1.value!! > 0) _ast1.value = _ast1.value?.minus(1)
    }

    fun setBloodSputum(i: Int) {
        cbac.cbac_bloodsputum_pos = i
        if (i == 1) _ast1.value = _ast1.value?.plus(1)
        else if (i == 2 && ast1.value!! > 0) _ast1.value = _ast1.value?.minus(1)
    }

    fun setFeverWks(i: Int) {
        cbac.cbac_fivermore_pos = i
        if (i == 1) _ast1.value = _ast1.value?.plus(1)
        else if (i == 2 && ast1.value!! > 0) _ast1.value = _ast1.value?.minus(1)
    }

    fun setLsWt(i: Int) {
        cbac.cbac_loseofweight_pos = i
        if (i == 1) _ast1.value = _ast1.value?.plus(1)
        else if (i == 2 && ast1.value!! > 0) _ast1.value = _ast1.value?.minus(1)
    }

    fun setNtSwets(i: Int) {
        cbac.cbac_nightsweats_pos = i
        if (i == 1) _ast1.value = _ast1.value?.plus(1)
        else if (i == 2 && ast1.value!! > 0) _ast1.value = _ast1.value?.minus(1)
    }

    fun setRecurrentCloudy(i: Int) {
        cbac.cbac_cloudy_posi = i
    }

    fun setRecurrentUlceration(i: Int) {
        cbac.cbac_uicers_pos = i
    }

    fun setDiffReading(i: Int) {
        cbac.cbac_diffreading_posi = i
    }

    fun setPainEyes(i: Int) {
        cbac.cbac_pain_ineyes_posi = i
    }

    fun setDiffHearing(i: Int) {
        cbac.cbac_diff_inhearing_posi = i
    }

    fun setRedEyes(i: Int) {
        cbac.cbac_redness_ineyes_posi = i
    }

    fun setBreathe(i: Int) {
        cbac.cbac_sortnesofbirth_pos = i
    }

    fun setHisFits(i: Int) {
        cbac.cbac_historyoffits_pos = i
    }

    fun setDiffMouth(i: Int) {
        cbac.cbac_difficultyinmouth_pos = i
    }

    fun setHealed(i: Int) {
        cbac.cbac_growth_in_mouth_posi = i
    }

    fun setVoice(i: Int) {
        cbac.cbac_toneofvoice_pos = i

    }

    fun setAnyGrowth(i: Int) {
        cbac.cbac_growth_in_mouth_posi = i
    }

    fun setAnyWhite(i: Int) {
        cbac.cbac_white_or_red_patch_posi = i
    }

    fun setPainChew(i: Int) {
        cbac.cbac_Pain_while_chewing_posi = i
    }

    fun setHyperPig(i: Int) {
        cbac.cbac_hyper_pigmented_patch_posi = i
    }

    fun setThickSkin(i: Int) {
        cbac.cbac_any_thickend_skin_posi = i
    }

    fun setNoduleSkin(i: Int) {
        cbac.cbac_nodules_on_skin_posi = i
    }

    fun setTing(i: Int) {
        cbac.cbac_tingling_palm_posi = i
    }


    fun setNumb(i: Int) {
        cbac.cbac_numbness_on_palm_posi = i
    }

    fun setClaw(i: Int) {
        cbac.cbac_clawing_of_fingers_posi = i
    }

    fun setTingNumb(i: Int) {
        cbac.cbac_tingling_or_numbness_posi = i
    }

    fun setCloseEyelid(i: Int) {
        cbac.cbac_inability_close_eyelid_posi = i
    }

    fun setHoldObj(i: Int) {
        cbac.cbac_diff_holding_obj_posi = i
    }

    fun setWeakFeet(i: Int) {
        cbac.cbac_weekness_in_feet_posi = i
    }

    fun setLumpB(i: Int) {
        cbac.cbac_lumpinbreast_pos = i
    }

    fun setNipple(i: Int) {
        cbac.cbac_blooddischage_pos = i
    }

    fun setBreast(i: Int) {
        cbac.cbac_changeinbreast_pos = i
    }

    fun setBlP(i: Int) {
        cbac.cbac_bleedingbtwnperiods_pos = i
    }

    fun setBlM(i: Int) {
        cbac.cbac_bleedingaftermenopause_pos = i
    }

    fun setBlI(i: Int) {
        cbac.cbac_bleedingafterintercourse_pos = i
    }

    fun setFoulD(i: Int) {
        cbac.cbac_foulveginaldischarge_pos = i
    }

    fun setUnsteady(i: Int) {
        cbac.cbac_feeling_unsteady_posi = i
        if (i == 1) _astMoic.value = _astMoic.value?.plus(1)
        else if (i == 2 && _astMoic.value!! > 0) _astMoic.value = _astMoic.value?.minus(1)
    }

    fun setPdRm(i: Int) {
        cbac.cbac_suffer_physical_disability_posi = i
        if (i == 1) _astMoic.value = _astMoic.value?.plus(1)
        else if (i == 2 && _astMoic.value!! > 0) _astMoic.value = _astMoic.value?.minus(1)
    }

    fun setNhop(i: Int) {
        cbac.cbac_needing_help_posi = i
        if (i == 1) _astMoic.value = _astMoic.value?.plus(1)
        else if (i == 2 && _astMoic.value!! > 0) _astMoic.value = _astMoic.value?.minus(1)
    }

    fun setForgetNames(i: Int) {
        cbac.cbac_forgetting_names_posi = i
        if (i == 1) _astMoic.value = _astMoic.value?.plus(1)
        else if (i == 2 && _astMoic.value!! > 0) _astMoic.value = _astMoic.value?.minus(1)
    }

    fun setFuelType(i: Int) {
        cbac.cbac_fuel_used_posi = i + 1

    }

    fun setOccExposure(i: Int) {
        cbac.cbac_occupational_exposure_posi = i + 1
    }

    fun setLi(i: Int) {
        cbac.cbac_little_interest_posi = i + 1
        _phq2LittleInterestScore.value = i
        cbac.cbac_little_interest_score = i
        _phq2TotalScore.value = _phq2LittleInterestScore.value!! + _phq2FeelDownDepScore.value!!

    }

    fun setFd(i: Int) {
        cbac.cbac_feeling_down_posi = i + 1
        _phq2FeelDownDepScore.value = i
        cbac.cbac_feeling_down_score = i
        _phq2TotalScore.value = _phq2LittleInterestScore.value!! + _phq2FeelDownDepScore.value!!
    }

    fun submitForm() {
        if (!dataValid()) {
            _state.value = State.MISSING_FIELD
            return
        }
        _state.value = State.SAVING
        cbac.total_score = _raTotalScore.value!!
        var flagForHrp = false
//        if (ben.genDetails?.reproductiveStatusId == 1 || ben.genDetails?.reproductiveStatusId == 2 || ben.genDetails?.reproductiveStatusId == 3) {
            //hrp related posibilities
            if ((ben.gender.genderName.lowercase() == "female") && (
                        cbac.cbac_foulveginaldischarge_pos == 1 ||
                                cbac.cbac_sufferingtb_pos == 1 ||
                                cbac.cbac_bleedingafterintercourse_pos == 1 ||
                                cbac.cbac_antitbdrugs_pos == 1 ||
                                cbac.cbac_tbhistory_pos == 1 ||
                                cbac.cbac_historyoffits_pos == 1 ||
                                cbac.cbac_growth_in_mouth_posi == 1 ||
                                cbac.cbac_numbness_on_palm_posi == 1 ||
                                cbac.cbac_clawing_of_fingers_posi == 1 ||
                                cbac.cbac_tingling_or_numbness_posi == 1 ||
                                cbac.cbac_inability_close_eyelid_posi == 1 ||
                                cbac.cbac_diff_holding_obj_posi == 1 ||
                                cbac.cbac_blooddischage_pos == 1 ||
                                cbac.cbac_weekness_in_feet_posi == 1 ||
                                cbac.cbac_sortnesofbirth_pos == 1 ||
                                cbac.cbac_coughing_pos == 1 ||
                                cbac.cbac_bloodsputum_pos == 1 ||
                                cbac.cbac_fivermore_pos == 1 ||
                                cbac.cbac_loseofweight_pos == 1 ||
                                cbac.cbac_nightsweats_pos == 1)
            ) {
                // hrp_displayTv.setVisibility(View.VISIBLE);
                flagForHrp = true
//                flagForHrp = when (ben.genDetails?.reproductiveStatusId) {
////                    1 -> {
////                        ben.nishchayPregnancyStatusPosition == 1
////                    }
//                    5, 4 -> {
//                        false
//                    }
//
//                    else -> {
//                        true
//                    }
//                }
            } else {
                // hrp_displayTv.setVisibility(View.GONE);
                flagForHrp = false
            }
//        }
        cbac.hrp_suspected = flagForHrp
        cbac.suspected_hrp = if (flagForHrp) "Yes" else "No"
//        ben.suspectedHrp = if (flagForHrp) "Yes" else "No"
        val flagForTb = (
                cbac.cbac_coughing_pos == 1 ||
                        cbac.cbac_familyhistory_posi == 1 ||
                        cbac.cbac_tbhistory_pos == 1 ||
                        cbac.cbac_bloodsputum_pos == 1 ||
                        cbac.cbac_fivermore_pos == 1 ||
                        cbac.cbac_loseofweight_pos == 1 ||
                        cbac.cbac_nightsweats_pos == 1 ||
                        cbac.cbac_antitbdrugs_pos == 1 ||
                        cbac.cbac_growth_in_mouth_posi == 1)
        cbac.suspected_tb = if (flagForTb) "Yes" else "No"
//        ben.suspectedTb = if (flagForTb) "Yes" else "No"
        cbac.suspected_ncd_diseases = ""
        //val diseasesList = mutableListOf<String>()

        if (flagForNcd || flagForPhq2) {
            cbac.ncd_confirmed = true
            cbac.ncd_suspected = "Yes"
            cbac.suspected_ncd = "Yes"
//            ben.suspectedNcd = "Yes"
//            ben.ncdPriority = 1
        } else {
            cbac.ncd_confirmed = false
            cbac.ncd_suspected = "No"
            cbac.suspected_ncd = "No"
//            ben.suspectedNcd = "No"
//            ben.ncdPriority = 0

        }
//        ben.cbacAvailable = true
//        if (ben.processed != "N") ben.processed = "U"
//        ben.syncState = SyncState.UNSYNCED


        //locationReocrd:
        cbac.Countyid = 1
        cbac.stateid = ben.state?.stateID ?: 0
        cbac.districtid = ben.district?.districtID ?: 0
//        cbac.districtname = ben.locationRecord
        cbac.villageid = ben.village?.districtBranchID ?: 0
        cbac.cbac_reg_id = ben.patient.beneficiaryRegID ?: 0

        viewModelScope.launch {
            val result = cbacRepo.saveCbacData(cbac, ben)
            if (result)
                _state.value = State.SAVE_SUCCESS
            else
                _state.value = State.SAVE_FAIL
        }


    }

    private fun dataValid(): Boolean {
        if (cbac.cbac_age_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_your_age)
            return false
        }
        if (cbac.cbac_smoke_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_smoke)
            return false
        }
        if (cbac.cbac_alcohol_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_consume_alcohol)
            return false
        }
        if (cbac.cbac_waist_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_waist)
            return false
        }
        if (cbac.cbac_pa_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_physical_activities)
            return false
        }
        if (cbac.cbac_familyhistory_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_heart_disease)
            return false
        }
        if (cbac.cbac_sufferingtb_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plsst)
            return false
        }
        if (cbac.cbac_antitbdrugs_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plsatb)
            return false
        }
        if (cbac.cbac_tbhistory_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plshtb)
            return false
        }
        if (cbac.cbac_coughing_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plscoughh)
            return false
        }
        if (cbac.cbac_bloodsputum_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plbls)
            return false
        }
        if (cbac.cbac_fivermore_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plsfever)
            return false
        }
        if (cbac.cbac_loseofweight_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plslweight)
            return false
        }
        if (cbac.cbac_nightsweats_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_plsnsw)
            return false
        }
        if (cbac.cbac_uicers_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_rul)
            return false
        }
        if (cbac.cbac_tingling_palm_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_rtps)
            return false
        }
        if (cbac.cbac_cloudy_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_cbv)
            return false
        }
        if (cbac.cbac_diffreading_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_dr)
            return false
        }
        if (cbac.cbac_pain_ineyes_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_pie)
            return false
        }
        if (cbac.cbac_redness_ineyes_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_rie)
            return false
        }
        if (cbac.cbac_diff_inhearing_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_dih)
            return false
        }
        if (cbac.cbac_sortnesofbirth_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_sob)
            return false
        }
        if (cbac.cbac_historyoffits_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_hif)
            return false
        }
        if (cbac.cbac_difficultyinmouth_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_diom)
            return false
        }
        if (cbac.cbac_growth_in_mouth_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_upgm)
            return false
        }
        if (cbac.cbac_toneofvoice_pos == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_tov)
            return false
        }
        if (cbac.cbac_white_or_red_patch_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_wrp)
            return false
        }
        if (cbac.cbac_Pain_while_chewing_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_pwh)
            return false
        }
        if (cbac.cbac_hyper_pigmented_patch_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_hpds)
            return false
        }
        if (cbac.cbac_any_thickend_skin_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_ts)
            return false
        }
        if (cbac.cbac_nodules_on_skin_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_ns)
            return false
        }
        if (cbac.cbac_numbness_on_palm_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_rnps)
            return false
        }
        if (cbac.cbac_clawing_of_fingers_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_cfhf)
            return false
        }
        if (cbac.cbac_tingling_or_numbness_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_tnhf)
            return false
        }
        if (cbac.cbac_inability_close_eyelid_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_ice)
            return false
        }
        if (cbac.cbac_diff_holding_obj_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_dhof)
            return false
        }
        if (cbac.cbac_weekness_in_feet_posi == 0) {
            missingFieldString = resources.getString(R.string.cbac_validation_wfdw)
            return false
        }
        if (ben.gender.genderName.lowercase() == "female") {
            if (cbac.cbac_lumpinbreast_pos == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_lb)
                return false
            }
            if (cbac.cbac_blooddischage_pos == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_bsdfn)
                return false
            }
            if (cbac.cbac_changeinbreast_pos == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_csb)
                return false
            }
            if (cbac.cbac_bleedingbtwnperiods_pos == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_bbp)
                return false
            }
            if (cbac.cbac_bleedingaftermenopause_pos == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_bam)
                return false
            }
            if (cbac.cbac_bleedingafterintercourse_pos == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_bai)
                return false
            }
            if (cbac.cbac_foulveginaldischarge_pos == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_fsvd)
                return false
            }
        }

        if (ben.patient.age!! >= 60) {
            if (cbac.cbac_feeling_unsteady_posi == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_fuwsw)
                return false
            }
            if (cbac.cbac_suffer_physical_disability_posi == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_sapd)
                return false
            }
            if (cbac.cbac_needing_help_posi == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_nhpa)
                return false
            }
            if (cbac.cbac_forgetting_names_posi == 0) {
                missingFieldString = resources.getString(R.string.cbac_validation_fn)
                return false
            }
        }
//        if (cbac.cbac_fuel_used_posi == 0) {
//            missingFieldString = context.getString(R.string.cbac_validation_fu)
//            return false
//        }
//        if (cbac.cbac_occupational_exposure_posi == 0) {
//            missingFieldString = context.getString(R.string.cbac_validation_ox)
//            return false
//        }
//        if (cbac.cbac_little_interest_posi == 0) {
//            missingFieldString = context.getString(R.string.cbac_validation_li)
//            return false
//        }
//        if (cbac.cbac_feeling_down_posi == 0) {
//            missingFieldString = context.getString(R.string.cbac_validation_fd)
//            return false
//        }
        return true
    }

    fun setFlagForPhQ2(b: Boolean) {
        flagForPhq2 = b
    }

    fun setFlagForNcd(b: Boolean) {
        flagForNcd = b

    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun setCollectSputum(i: Int) {
        cbac.cbac_sputemcollection = i.toString()
    }

    fun getCollectSputum(): String {
        return cbac.cbac_sputemcollection ?: "0"
    }

    fun setTraceAllMembers(i: Int) {
        cbac.cbac_tracing_all_fm = i.toString()
    }

    fun setReferMoic(i: Int) {
        cbac.cbac_referpatient_mo = i.toString()
    }

    fun setFillDate(date: Long) {
        cbac.fillDate = date
    }

    private fun getLocalizedResources(context: Context, currentLanguage: Languages): Resources {
        val desiredLocale = Locale(currentLanguage.symbol)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(desiredLocale)
        val localizedContext: Context = context.createConfigurationContext(conf)
        return localizedContext.resources
    }

}