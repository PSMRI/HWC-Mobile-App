package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import com.squareup.moshi.JsonClass

/**
 * Reusable set of referral & follow-up columns shared by every assessment entity.
 * Embed with `@Embedded` in Room entities and delegate `ReferralFollowUpModel` to this instance.
 */
@JsonClass(generateAdapter = true)
data class ReferralFollowUpFields(

    @ColumnInfo(name = "referral_required")
    override var referralRequired: Boolean? = null,

    @ColumnInfo(name = "referral_level")
    override var referralLevel: String? = null,

    @ColumnInfo(name = "reason_for_referral")
    override var reasonForReferral: String? = null,

    @ColumnInfo(name = "follow_up_required")
    override var followUpRequired: Boolean? = null,

    @ColumnInfo(name = "follow_up_date")
    override var followUpDate: String? = null,

    @ColumnInfo(name = "case_status")
    override var caseStatus: String? = null,

    @ColumnInfo(name = "date_of_death")
    override var dateOfDeath: String? = null,

    @ColumnInfo(name = "remarks")
    override var remarks: String? = null

) : ReferralFollowUpModel

