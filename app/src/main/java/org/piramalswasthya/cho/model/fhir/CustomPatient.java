package org.piramalswasthya.cho.model.fhir;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

//@ResourceDef(name="Patient")
public class CustomPatient extends Patient {

    @Child(name="petName")
//    @Extension(url="http://example.com/dontuse#petname", definedLocally=false, isModifier=false)
    @Description(shortDefinition="The name of the patient's favourite pet")
    private StringType myPetName;






}
