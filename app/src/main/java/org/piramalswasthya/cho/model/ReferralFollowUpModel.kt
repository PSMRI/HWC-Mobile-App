package org.piramalswasthya.cho.model

interface ReferralFollowUpModel {
    var referralRequired: Boolean?
    var referralLevel: String?
    var reasonForReferral: String?
    var followUpRequired: Boolean?
    var followUpDate: String?

    var caseStatus: String?

    var dateOfDeath: String?

    var remarks: String?
}