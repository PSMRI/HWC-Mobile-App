package org.piramalswasthya.cho.ui.register_patient_activity.scanAadhaar

import com.journeyapps.barcodescanner.CaptureActivity
import org.piramalswasthya.cho.utils.ImgUtils

class CaptureAct : CaptureActivity() {
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        ImgUtils.canFinishActivity = true
        super.onBackPressed()
    }
}
