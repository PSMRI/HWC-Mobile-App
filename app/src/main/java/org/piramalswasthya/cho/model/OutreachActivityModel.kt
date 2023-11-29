package org.piramalswasthya.cho.model

import org.piramalswasthya.cho.database.room.SyncState
import java.util.Date

data class OutreachActivityModel(
    var dataOfActivity: Date? = null,
    var activityName: String? = null,
    var eventDesc: String? = null,
    var noOfParticipant: Int? = null,
    var photos: Array<String>? = null,
)