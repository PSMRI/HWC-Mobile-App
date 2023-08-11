package org.piramalswasthya.cho.ui.commons

import androidx.compose.ui.text.toLowerCase
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.Type

class FhirExtension {

    companion object {

        private const val baseUrl : String = "http://hl7.org/fhir/StructureDefinition/Patient#Patient"

        fun getExtenstion(url: String, value: Type) : Extension {
            val extension = Extension();
            extension.url = url
            extension.setValue(value)
            return extension
        }

        fun getUrl(variableName: String) : String{
            var url = baseUrl;
            url += ".$variableName";
            return url
        }

        fun getStringType(value : String) : StringType {
            val str = StringType(value);
            return str;
        }

        fun getCoding(code : String, display: String) : Coding {
            val cdt = Coding();
            cdt.code = code
            cdt.display = display
            return cdt;
        }

    }

}