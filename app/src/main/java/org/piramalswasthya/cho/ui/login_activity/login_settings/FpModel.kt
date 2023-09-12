package org.piramalswasthya.cho.ui.login_activity.login_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.model.FingerPrint
import org.piramalswasthya.cho.model.FingerPrintToServer
import org.piramalswasthya.cho.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FpModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {
    private var userName = ""
    private var rightThumb = ""
    private var rightIndexFinger = ""
    private var leftThumb = ""
    private var leftIndexFinger = ""
    private var fingerPrintDataForServer: FingerPrintToServer? = null
    fun submitFPData(fpList: List<FingerPrint>) {
        // creating finger print object for server
        userName = fpList[0].userName!!
        for (item in fpList) {
            if (item.fingerType!! == "Right Thumb") rightThumb = item.fpVal!!
            else if (item.fingerType!! == "Right Index Finger") rightIndexFinger = item.fpVal!!
            else if (item.fingerType!! == "Left Thumb") leftThumb = item.fpVal!!
            else leftIndexFinger = item.fpVal!!
        }
        fingerPrintDataForServer = FingerPrintToServer(
            1,
            userName,
            rightThumb,
            rightIndexFinger,
            leftThumb,
            leftIndexFinger
        )

        // coroutine scope for saving finger print to local db
        viewModelScope.launch {
            try {
                userRepo.insertFPDataToLocalDB(fpList)
            } catch (e: Exception) {
                Timber.d("Error in inserting Finger Print Data insertFPDataToLocalDB()$e")
            }
        }
    }
}