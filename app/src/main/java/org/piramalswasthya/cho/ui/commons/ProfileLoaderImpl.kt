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

    private val elementDefinition = ElementDefinition()

    private fun getElementDefinition(): ElementDefinition{
        elementDefinition.id = "Patient:extended"
        elementDefinition.path = "Patient.extension"
        return elementDefinition
    }

    private fun getStructureDefinition(): StructureDefinition{
        structureDefinition.snapshot.element.clear()
        structureDefinition.snapshot.element.add(getElementDefinition())
        return structureDefinition
    }

    override fun loadProfile(url: CanonicalType): StructureDefinition? {
        return getStructureDefinition()
    }

}