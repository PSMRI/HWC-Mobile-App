package org.piramalswasthya.cho.model

import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.utils.formattedDate
import java.io.Serializable
import java.util.Date

data class OutreachActivityModel(
    var dateOfActivity: Date? = null,
    var activityName: String? = null,
    var eventDesc: String? = null,
    var noOfParticipant: Int? = null,
    var img1: String? = null,
    var img2: String? = null,
)

data class OutreachActivityNetworkModel(
    var activityId: Int? = null,
    var userId: Int? = null,
    var activityDate: String? = null,
    var activityName: String? = null,
    var eventDescription: String? = null,
    var noOfParticipants: Int? = null,
    var img1: String? = null,
    var img2: String? = null,
) : Serializable{
    constructor(user: UserDomain, outreachActivityModel: OutreachActivityModel) : this(
        null,
        user.userId,
        outreachActivityModel.dateOfActivity?.formattedDate(),
        outreachActivityModel.activityName,
        outreachActivityModel.eventDesc,
        outreachActivityModel.noOfParticipant,
        outreachActivityModel.img1,
        outreachActivityModel.img2,
    )
}

data class FormattedDate(
    var date: Int = 0,
    var month: Int = 0,
    var year: Int = 0
)
