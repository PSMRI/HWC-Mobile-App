package org.piramalswasthya.cho.ui.commons

import com.google.android.fhir.datacapture.mapping.ProfileLoader
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.ElementDefinition
import org.hl7.fhir.r4.model.StructureDefinition

// definition : "http://hl7.org/fhir/StructureDefinition/Patient#Patient.extensionname"
// path : "Patient.extension"
// id : ":extensionname"

class ProfileLoaderImpl : ProfileLoader {

    private val structureDefinition = StructureDefinition()

    private var elementDefinition = ElementDefinition()

    private fun getElementDefinition(): ElementDefinition{
        elementDefinition.id = "Patient:extensionname"
        elementDefinition.path = "Patient.extension"
        return elementDefinition
    }

    private fun getStructureDefinition(): StructureDefinition{
        structureDefinition.snapshot.element.clear()

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":registrarState"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":registrarDistrict"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":registrarTaluk"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":registrarStreet"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":abhaGenerationMode"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":govtIdType"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":govtIdNumber"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":govtHealthProgramType"
        structureDefinition.snapshot.element.add(elementDefinition)

        elementDefinition = ElementDefinition()
        elementDefinition.path = "Patient.extension"
        elementDefinition.id = ":govtHealthProgramId"
        structureDefinition.snapshot.element.add(elementDefinition)

        return structureDefinition
    }

    override fun loadProfile(url: CanonicalType): StructureDefinition? {
        return getStructureDefinition()
    }

}