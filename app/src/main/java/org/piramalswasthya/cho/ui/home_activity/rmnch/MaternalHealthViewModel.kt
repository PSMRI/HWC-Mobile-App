package org.piramalswasthya.cho.ui.home_activity.rmnch

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.repositories.PatientRepo
import javax.inject.Inject

@HiltViewModel
class MaternalHealthViewModel @Inject constructor(
    private val patientRepo: PatientRepo
) : ViewModel() {
    
    // Delivery Outcome List - Women who have delivered
    val deliveryOutcomeList: Flow<List<PatientDisplayWithVisitInfo>> = 
        patientRepo.getDeliveredWomenList()
    
    // Infant Registration List - Women with babies needing registration
    val infantRegList: Flow<List<PatientDisplayWithVisitInfo>> = 
        patientRepo.getPNCActiveWomenWithBabiesList()
}
