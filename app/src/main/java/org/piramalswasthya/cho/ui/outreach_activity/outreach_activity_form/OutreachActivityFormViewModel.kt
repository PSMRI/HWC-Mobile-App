package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_form

import android.content.Context
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.cho.model.OutreachActivityModel
import org.piramalswasthya.cho.repositories.BenFlowRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PatientVisitInfoSyncRepo
import org.piramalswasthya.cho.repositories.UserRepo
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OutreachActivityFormViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
) : ViewModel() {

    var dataOfActivity: MutableLiveData<Date?> = MutableLiveData<Date?>(null)
    var activityName: MutableLiveData<String?> = MutableLiveData<String?>()
    var eventDesc: MutableLiveData<String?> = MutableLiveData<String?>()
    var noOfParticipant: MutableLiveData<Int?> = MutableLiveData<Int?>()
    var photos: Array<String>? = null

    var dateOfActivityDisplay: MutableLiveData<String?> = MutableLiveData<String?>(null)

    var activityList: List<String> = listOf(
        "School Visit",
        "Special Screening Camp",
        "Community Meeting",
        "Yoga",
        "Meditation",
        "Awareness Activity",
        "Other"
    )

    val outreachActivityModel: OutreachActivityModel = OutreachActivityModel()

    fun onActivityNameItemClick (parent: AdapterView<*>, view: View, position: Int, id: Long) {
        activityName.value = activityList[position]
    }

}