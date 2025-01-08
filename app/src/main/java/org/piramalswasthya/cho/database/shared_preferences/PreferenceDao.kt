package org.piramalswasthya.cho.database.shared_preferences

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.LocationData
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.UserBlockDetails
import org.piramalswasthya.cho.model.UserBlockDetailsData
import org.piramalswasthya.cho.model.UserDistrictDetails
import org.piramalswasthya.cho.model.UserDistrictDetailsData
import org.piramalswasthya.cho.model.UserNetwork
import org.piramalswasthya.cho.model.UserStateDetails
import org.piramalswasthya.cho.model.UserStateDetailsData
import org.piramalswasthya.cho.model.UserVanSpDetails
import org.piramalswasthya.cho.model.UserVanSpDetailsData
import org.piramalswasthya.cho.model.UserVillageDetails
import org.piramalswasthya.cho.model.UserVillageDetailsData
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceDao @Inject constructor(@ApplicationContext private val context: Context) {

    private val pref = PreferenceManager.getInstance(context)

    @RequiresApi(Build.VERSION_CODES.O)
    val date = LocalDate.of(2023, 11, 1)

    @RequiresApi(Build.VERSION_CODES.O)
    val epochTimestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


    fun getPrimaryApiToken(): String? {
        val prefKey = context.getString(R.string.PREF_primary_API_KEY)
        return pref.getString(prefKey, null)
    }
//
    fun registerPrimaryApiToken(token: String) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_primary_API_KEY)
        editor.putString(prefKey, token)
        editor.apply()
    }

    fun setUserRoles(roles: String) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.USER_ROLES)
        editor.putString(prefKey, roles)
        editor.apply()
    }

    fun getUserRoles(): String? {
        val prefKey = context.getString(R.string.USER_ROLES)
        return pref.getString(prefKey, null)
    }
    fun getLoginType(): String? {
        val prefKey = context.getString(R.string.User_Login_Type)
        return pref.getString(prefKey, null)
    }


    fun setSwitchRoles(roles: String) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.SWITCH_USER_ROLES)
        editor.putString(prefKey, roles)
        editor.apply()
    }

     fun isUserRegistrar():Boolean{
         val rolesArray = getUserRoles()?.split(",")
         if(rolesArray != null){
             return rolesArray.contains("Registrar")
         }
         return false;
     }

    fun isUserStaffNurseOrNurse():Boolean{
        val rolesArray = getUserRoles()?.split(",")
        if(rolesArray != null){
            return rolesArray.contains("Staff Nurse") || rolesArray.contains("Nurse")
        }
        return false;
    }

    fun isUserDoctorOrMO():Boolean{
        val rolesArray = getUserRoles()?.split(",")
        if(rolesArray != null){
            return rolesArray.contains("Doctor") || rolesArray.contains("MO")
        }
        return false;
    }

    fun isUserLabTechnician():Boolean{
        val rolesArray = getUserRoles()?.split(",")
        if(rolesArray != null){
            return rolesArray.contains("Lab Technician")
        }
        return false;
    }

    fun isUserPharmacist():Boolean{
        val rolesArray = getUserRoles()?.split(",")
        if(rolesArray != null){
            return rolesArray.contains("Pharmacist")
        }
        return false;
    }

    fun isUserCHO():Boolean{
        val rolesArray = getUserRoles()?.split(",")
        if(rolesArray != null){
            return rolesArray.contains("CHO")
        }
        return false;
    }

    fun setUserLoginType(str:String?){
        val editor = pref.edit()
        val prefKey = context.getString(R.string.User_Login_Type)
        editor.putString(prefKey, str)
        editor.apply()
    }

    fun isLoginTypeOutReach():Boolean{
        val type = getLoginType()
        if(type != null)
            return type.contains("OUTREACH")
      return false
    }

    fun getSwitchRole(): String? {
        val prefKey = context.getString(R.string.SWITCH_USER_ROLES)
        return pref.getString(prefKey, null)
    }

    fun isRegistrarSelected(): Boolean {
        return getSwitchRole() == "Registrar"
    }

    fun isNurseSelected(): Boolean {
        return getSwitchRole() == "Nurse"
    }

    fun isDoctorSelected(): Boolean {
        return getSwitchRole() == "Doctor"
    }

    fun isLabSelected(): Boolean {
        return getSwitchRole() == "Lab Technician"
    }

    fun isPharmaSelected(): Boolean {
        return getSwitchRole() == "Pharmacist"
    }




    fun getLastBenflowSyncTime(): String {
        val prefKey = context.getString(R.string.last_benflow_sync_time)
        return pref.getString(prefKey, null) ?: DateTimeUtil.formatCustDateAndTime(epochTimestamp)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLastBenflowSyncTime(currTimeStamp: Long){
        val prefKey = context.getString(R.string.last_benflow_sync_time)
        val editor = pref.edit()
       editor.putString(prefKey, DateTimeUtil.formatCustDateAndTime(currTimeStamp))
        editor.apply()
    }

    fun getLastPatientSyncTime(): String {
        val prefKey = context.getString(R.string.last_patient_sync_time)
        return pref.getString(prefKey, null) ?: DateTimeUtil.formatCustDateAndTime(epochTimestamp)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLastPatientSyncTime(currTimeStamp: Long){
        val prefKey = context.getString(R.string.last_patient_sync_time)
        val editor = pref.edit()


        editor.putString(prefKey, DateTimeUtil.formatCustDateAndTime(currTimeStamp))
        editor.apply()
    }

    fun getLastSyncTime(): String {
        val prefKey = context.getString(R.string.last_sync_time)
        return pref.getString(prefKey, null) ?: DateTimeUtil.formatCustDateAndTime(epochTimestamp)
    }

    fun registerLoginCred(userName: String,password: String) {
        val editor = pref.edit()
        val prefUserKey = context.getString(R.string.PREF_rem_me_uname)
        val prefPasswordKey = context.getString(R.string.password_local_saved)
        editor.putString(prefUserKey, userName)
        editor.putString(prefPasswordKey, password)
        editor.apply()
    }
    fun getRememberedUserName(): String? {
        val key = context.getString(R.string.PREF_rem_me_uname)
        return pref.getString(key, null)
    }
    fun getRememberedPassword(): String? {
        val prefPasswordKey = context.getString(R.string.password_local_saved)
        return pref.getString(prefPasswordKey, null)
    }

fun registerEsanjeevaniCred(userName: String,password: String) {
    val editor = pref.edit()
    val prefUserKey = context.getString(R.string.esanjeevaniusername_local_saved)
    val prefPasswordKey = context.getString(R.string.esanjeevanipassword_local_saved)
    editor.putString(prefUserKey, userName)
    editor.putString(prefPasswordKey, password)
    editor.apply()
}
    fun getEsanjeevaniUserName(): String? {
        val key = context.getString(R.string.esanjeevaniusername_local_saved)
        return pref.getString(key, null)
    }
    fun getEsanjeevaniPassword(): String? {
        val prefPasswordKey = context.getString(R.string.esanjeevanipassword_local_saved)
        return pref.getString(prefPasswordKey, null)
    }
    fun deleteEsanjeevaniCreds() {
        val editor = pref.edit()
        val prefUserKeyEs = context.getString(R.string.esanjeevaniusername_local_saved)
        val prefPasswordKeyEs = context.getString(R.string.esanjeevanipassword_local_saved)
        editor.remove(prefUserKeyEs)
        editor.remove(prefPasswordKeyEs)
        editor.apply()
    }
    fun deleteLoginCred() {
        val editor = pref.edit()
        val prefUserKey = context.getString(R.string.PREF_rem_me_uname)
        val prefPasswordKey = context.getString(R.string.password_local_saved)
        editor.remove(prefUserKey)
        editor.remove(prefPasswordKey)
        editor.apply()
    }
    fun registerUser(user: UserNetwork) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_user_entry)
        val userJson = Gson().toJson(user)
        editor.putString(prefKey, userJson)
        editor.apply()
    }

    fun getLoggedInUser(): UserNetwork? {
        val prefKey = context.getString(R.string.PREF_user_entry)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserNetwork::class.java)
    }
    fun saveUserLocationData(location: LocationData) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_location_data)
        val locJson = Gson().toJson(location)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getUserLocationData(): LocationData? {
        val prefKey = context.getString(R.string.pref_location_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, LocationData::class.java)
    }

    fun saveVanData(van: UserVanSpDetails) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_van_data)
        val locJson = Gson().toJson(van)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getVanData(): UserVanSpDetails? {
        val prefKey = context.getString(R.string.pref_van_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserVanSpDetails::class.java)
    }

    fun saveServicePointData(van: UserVanSpDetails) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_service_point_data)
        val locJson = Gson().toJson(van)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getServicePointData(): UserVanSpDetails? {
        val prefKey = context.getString(R.string.pref_service_point_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserVanSpDetails::class.java)
    }

    fun saveStateData(van: UserStateDetails) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_state_data)
        val locJson = Gson().toJson(van)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getStateData(): UserStateDetails? {
        val prefKey = context.getString(R.string.pref_state_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserStateDetails::class.java)
    }

    fun saveDistrictData(van: UserDistrictDetails) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_district_data)
        val locJson = Gson().toJson(van)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getDistrictData(): UserDistrictDetails? {
        val prefKey = context.getString(R.string.pref_district_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserDistrictDetails::class.java)
    }

    fun saveBlockData(van: UserBlockDetails) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_block_data)
        val locJson = Gson().toJson(van)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getBlockData(): UserBlockDetails? {
        val prefKey = context.getString(R.string.pref_block_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserBlockDetails::class.java)
    }

    fun saveVillageData(van: UserVillageDetails) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_village_data)
        val locJson = Gson().toJson(van)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getVillageData(): UserVillageDetails? {
        val prefKey = context.getString(R.string.pref_village_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserVillageDetails::class.java)
    }

    fun saveUserVanSpDetailsData(userVanSpDetails: UserVanSpDetailsData) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_location_data)
        val locJson = Gson().toJson(userVanSpDetails)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getUserVanSpDetailsData(): UserVanSpDetailsData? {
        val prefKey = context.getString(R.string.pref_location_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserVanSpDetailsData::class.java)
    }

    fun saveUserStateDetailsData(userStateDetails: UserStateDetailsData) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_location_data)
        val locJson = Gson().toJson(userStateDetails)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getUserStateDetailsData(): UserStateDetailsData? {
        val prefKey = context.getString(R.string.pref_location_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserStateDetailsData::class.java)
    }

    fun saveUserDistrictDetailsData(userDistrictDetails: UserDistrictDetailsData) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_location_data)
        val locJson = Gson().toJson(userDistrictDetails)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getUserDistrictDetailsData(): UserDistrictDetailsData? {
        val prefKey = context.getString(R.string.pref_location_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserDistrictDetailsData::class.java)
    }

    fun saveUserBlockDetailsData(userBlockDetails: UserBlockDetailsData) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_location_data)
        val locJson = Gson().toJson(userBlockDetails)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getUserBlockDetailsData(): UserBlockDetailsData? {
        val prefKey = context.getString(R.string.pref_location_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserBlockDetailsData::class.java)
    }

    fun saveUserVillageDetailsData(userVillageDetails: UserVillageDetailsData) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.pref_location_data)
        val locJson = Gson().toJson(userVillageDetails)
        editor.putString(prefKey, locJson)
        editor.apply()
    }
    fun getUserVillageDetailsData(): UserVillageDetailsData? {
        val prefKey = context.getString(R.string.pref_location_data)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, UserVillageDetailsData::class.java)
    }

    fun saveSetLanguage(language: Languages) {
        val key = context.getString(R.string.PREF_current_saved_language)
        val editor = pref.edit()
        editor.putString(key, language.symbol)
        editor.apply()
    }

    fun getCurrentLanguage(): Languages {
        val key = context.getString(R.string.PREF_current_saved_language)
        return when (pref.getString(key, null)) {
            Languages.ENGLISH.symbol -> Languages.ENGLISH
            Languages.KANNADA.symbol -> Languages.KANNADA
            else -> Languages.ENGLISH
        }
    }

    fun savePublicKeyForAbha(publicKey: String) {
        val key = "AUTH_CERT"
        val editor = pref.edit()
        editor.putString(key, publicKey)
        editor.apply()
    }

    fun getPublicKeyForAbha(): String? {
        val key = "AUTH_CERT"
        return pref.getString(key, null)
    }
}