package org.piramalswasthya.cho.fhir_utils

import com.google.android.fhir.datacapture.mapping.ProfileLoader
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.ElementDefinition
import org.hl7.fhir.r4.model.StructureDefinition
import org.piramalswasthya.cho.fhir_utils.extension_names.*


// definition : "http://hl7.org/fhir/StructureDefinition/Patient#Patient.extensionname"
// path : "Patient.extension"
// id : ":extensionname"

class ProfileLoaderPatient : ProfileLoader {

    private val structureDefinition = StructureDefinition()

    init {
        structureDefinition.snapshot.element.add(getElementDefinition(registrarState))
        structureDefinition.snapshot.element.add(getElementDefinition(registrarDistrict))
        structureDefinition.snapshot.element.add(getElementDefinition(registrarTaluk))
        structureDefinition.snapshot.element.add(getElementDefinition(registrarStreet))
        structureDefinition.snapshot.element.add(getElementDefinition(abhaGenerationMode))
        structureDefinition.snapshot.element.add(getElementDefinition(govtIdType))
        structureDefinition.snapshot.element.add(getElementDefinition(govtIdNumber))
        structureDefinition.snapshot.element.add(getElementDefinition(govtHealthProgramType))
        structureDefinition.snapshot.element.add(getElementDefinition(govtHealthProgramId))
    }

    private fun getElementDefinition(extensionName: String): ElementDefinition{
        var elementDefinition = ElementDefinition()
        elementDefinition.id = ":$extensionName"
        elementDefinition.path = "Patient.extension"
        return elementDefinition
    }

    override fun loadProfile(url: CanonicalType): StructureDefinition {
        return structureDefinition;
    }

}