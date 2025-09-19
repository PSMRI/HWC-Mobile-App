package org.piramalswasthya.cho.model

data class PrescriptionUpsync(
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
    constructor(user: UserDomain?, prescription: PrescriptionWithItemMasterAndDrugFormMaster) : this(//item master list
        null,
        prescription.itemId,
        prescription.itemName,
        "${prescription.strength}${prescription.unitOfMeasurement}",
        prescription.itemFormName,
        prescription.itemFormID,
        getDosage(prescription.itemFormName), // nullable
        1, // hard coded
        prescription.frequency,
        prescription.duration?.toInt(),
        "Oral", // hard coded
        "${prescription.duration?.toInt()} ${prescription.unit}",
        prescription.unit,

//        prescription.prescription.itemId,
//        prescription.itemMaster?.itemName,
//        "${prescription.itemMaster?.strength}${prescription.itemMaster?.unitOfMeasurement}",
//        prescription.drugMaster?.itemFormName,
//        prescription.itemMaster?.itemFormID,
//        "10 ml", // nullable
//        1, // hard coded
//        prescription.prescription.frequency,
//        prescription.prescription.duration?.toInt(),
//        "Oral", // hard coded
//        "${prescription.prescription.duration?.toInt()} ${prescription.prescription.unit}",
//        prescription.prescription.unit,
        prescription.instructions,
        null,
        null,
        user?.userName,
        user?.vanId,
        user?.parkingPlaceId,
        true
    )
}

fun getDosage(form: String): String?{
    if(form == "Tablet")
        return "One Tab"
    if(form == "Syrup")
        return "10 ml"
    return null;
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