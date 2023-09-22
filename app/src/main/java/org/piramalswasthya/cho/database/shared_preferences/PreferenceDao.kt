package org.piramalswasthya.cho.database.shared_preferences

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.UserNetwork
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceDao @Inject constructor(@ApplicationContext private val context: Context) {

    private val pref = PreferenceManager.getInstance(context)

    @RequiresApi(Build.VERSION_CODES.O)
    val date = LocalDate.of(2023, 9, 11)

    @RequiresApi(Build.VERSION_CODES.O)
    val epochTimestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


//    fun getD2DApiToken(): String? {
//        val prefKey = context.getString(R.string.PREF_D2D_API_KEY)
//        return pref.getString(prefKey, null)
//    }
//
//    fun registerD2DApiToken(token: String) {
//        val editor = pref.edit()
//        val prefKey = context.getString(R.string.PREF_D2D_API_KEY)
//        editor.putString(prefKey, token)
//        editor.apply()
//    }
//
//    fun deletePrimaryApiToken() {
//        val editor = pref.edit()
//        val prefKey = context.getString(R.string.PREF_primary_API_KEY)
//        editor.remove(prefKey)
//        editor.apply()
//    }

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

    fun isUserDoctor(): Boolean {
        val rolesArray = getUserRoles()?.split(",")
        if(rolesArray != null){
            return rolesArray.contains("Doctor")
        }
        return false;
    }


    fun saveLoginSettingsRecord(loginSettingsData: LoginSettingsData) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.login_settings)
        val loginSettingsJson = Gson().toJson(loginSettingsData)
        editor.putString(prefKey, loginSettingsJson)
        editor.apply()
    }

    fun getLoginSettingsRecord(): LoginSettingsData? {
        val prefKey = context.getString(R.string.login_settings)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, LoginSettingsData::class.java)
    }

    fun getLastSyncTime(): String {
        val prefKey = context.getString(R.string.last_sync_time)
        return pref.getString(prefKey, null) ?: DateTimeUtil.formatCustDateAndTime(epochTimestamp)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLastSyncTime(timestamp: Long){
        val prefKey = context.getString(R.string.last_sync_time)
        val editor = pref.edit()
        editor.putString(prefKey, DateTimeUtil.formatCustDateAndTime(timestamp))
        editor.apply()
    }

//    fun deleteD2DApiToken() {
//        val editor = pref.edit()
//        val prefKey = context.getString(R.string.PREF_primary_API_KEY)
//        editor.remove(prefKey)
//        editor.apply()
//    }

    fun registerLoginCred(userName: String) {
        val editor = pref.edit()
        val prefUserKey = context.getString(R.string.PREF_rem_me_uname)
        editor.putString(prefUserKey, userName)
        editor.apply()
    }
    fun getRememberedUserName(): String? {
        val key = context.getString(R.string.PREF_rem_me_uname)
        return pref.getString(key, null)
    }
//
//    fun deleteForLogout() {
//        pref.edit().clear().apply()
//    }
//
    fun deleteLoginCred() {
        val editor = pref.edit()
        val prefUserKey = context.getString(R.string.PREF_rem_me_uname)
        editor.remove(prefUserKey)
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

//

//
//    fun getRememberedPassword(): String? {
//        val key = context.getString(R.string.PREF_rem_me_pwd)
//        return pref.getString(key, null)
//    }
//    fun getRememberedState(): String? {
//        val key = context.getString(R.string.PREF_rem_me_state)
//        return pref.getString(key, null)
//    }
//
//    fun saveLocationRecord(locationRecord: LocationRecord) {
//        val editor = pref.edit()
//        val prefKey = context.getString(R.string.PREF_location_record_entry)
//        val locationRecordJson = Gson().toJson(locationRecord)
//        editor.putString(prefKey, locationRecordJson)
//        editor.apply()
//    }
//    fun getLocationRecord(): LocationRecord? {
//        val prefKey = context.getString(R.string.PREF_location_record_entry)
//        val json = pref.getString(prefKey, null)
//        return Gson().fromJson(json, LocationRecord::class.java)
//    }
//
//    fun setLastSyncedTimeStamp(lastSaved: Long) {
//        val editor = pref.edit()
//        val prefKey = context.getString(R.string.PREF_full_load_pull_progress)
//        editor.putLong(prefKey, lastSaved)
//        editor.apply()
//    }
//
//    fun getLastSyncedTimeStamp(): Long {
//        val prefKey = context.getString(R.string.PREF_full_load_pull_progress)
//        return pref.getLong(prefKey, 1603132200000)
//    }
//
//    fun setFirstSyncLastSyncedPage(page: Int) {
//        val editor = pref.edit()
//        val prefKey = context.getString(R.string.PREF_first_pull_amrit_last_synced_page)
//        editor.putInt(prefKey, page)
//        editor.apply()
//    }
//
//    fun getFirstSyncLastSyncedPage(): Int {
//        val prefKey = context.getString(R.string.PREF_first_pull_amrit_last_synced_page)
//        return pref.getInt(prefKey, 0)
//    }
//
//
//    fun saveSetLanguage(language: Languages) {
//        val key = context.getString(R.string.PREF_current_saved_language)
//        val editor = pref.edit()
//        editor.putString(key, language.symbol)
//        editor.apply()
//    }
//
    fun getCurrentLanguage(): Languages {
        val key = context.getString(R.string.PREF_current_saved_language)
        return when (pref.getString(key, null)) {
            Languages.ASSAMESE.symbol -> Languages.ASSAMESE
            Languages.HINDI.symbol -> Languages.HINDI
            Languages.ENGLISH.symbol -> Languages.ENGLISH
            else -> Languages.ENGLISH
        }
    }
//
//    fun saveProfilePicUri(uri: Uri) {
//        val key = context.getString(R.string.PREF_current_dp_uri)
//
//        val editor = pref.edit()
//        editor.putString(key, uri.toString())
//        editor.apply()
//        Timber.d("Saving profile pic @ $uri")
//    }
//
//    fun getProfilePicUri(): Uri? {
//        val key = context.getString(R.string.PREF_current_dp_uri)
//        val uriString = pref.getString(key, null)
//        return uriString?.let { Uri.parse(it) }
//    }
//
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