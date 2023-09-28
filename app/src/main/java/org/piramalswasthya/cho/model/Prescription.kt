package org.piramalswasthya.cho.model

data class Prescription(
    val id: Int?,
    val drugID: Int?,
    val drugName: String?,
    val drugStrength: String?,
    val formName: String?,
    val formID: Int?,
    val dose: String?,
    val qtyPrescribed: Int?,
    val frequency: String?,
    val duration: Int?,
    val route: String?,
    val durationView: String?,
    val unit: String?,
    val instructions: String?,
    val sctCode: String?,
    val sctTerm: String?,
    val createdBy: String?,
    val vanID: Int?,
    val parkingPlaceID: Int?,
    val isEDL: Boolean?,
){
    constructor(user: UserDomain?) : this(
        null,
        146,
        "Paracetamol",
        "125ml",
        "Syrup",
        3,
        "10 ml", // nullable
        1, // hard coded
        "Once Daily(OD)",
        3,
        "Oral", // hard coded
        "3 Day(s)",
        "Day(s)",
        null,
        null,
        null,
        user?.userName,
        user?.vanId,
        user?.parkingPlaceId,
        true
    )
}

//"id": null,
//"drugID": 146,
//"drugName": "Paracetamol",
//"drugStrength": "125ml",
//"formName": "Syrup",
//"formID": 3,
//"dose": "10 ml",
//"qtyPrescribed": 1,
//"frequency": "Once Daily(OD)",
//"duration": 3,
//"route": "Oral",
//"durationView": "3 Day(s)",
//"unit": "Day(s)",
//"instructions": null,
//"sctCode": null,
//"sctTerm": null,
//"createdBy": "Sanjay",
//"vanID": 168,
//"parkingPlaceID": 83,
//"isEDL": true