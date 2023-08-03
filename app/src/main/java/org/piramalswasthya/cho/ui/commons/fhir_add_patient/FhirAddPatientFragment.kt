package org.piramalswasthya.cho.ui.commons.fhir_add_patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.DomainResource
import org.hl7.fhir.r4.model.Patient

import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentFhirAddPatientBinding
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.FhirQuestionnaireService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity


/** A fragment class to show patient registration screen. */

@AndroidEntryPoint
class FhirAddPatientFragment : Fragment(R.layout.fragment_fhir_add_patient), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentFhirAddPatientBinding? = null

    private val binding: FragmentFhirAddPatientBinding
        get() = _binding!!

    override val viewModel: FhirAddPatientViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "new-patient-registration-paginated.json"

//    override val jsonFile : String = "dob_mapping.json"

//    override val jsonFile : String = "test_exp.json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFhirAddPatientBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContainerId = binding.fragmentContainer.id
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observeEntitySaveAction("Inputs are missing.", "Patient is saved.")
//        viewModel.patient = Patient()
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_fhir_add_patient;
    }

    override fun onSubmitAction() {
        saveEntity()
//        navigateNext()
    }

    override fun onCancelAction() {
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun navigateNext() {
        findNavController().navigate(
            FhirAddPatientFragmentDirections.actionFhirAddPatientFragmentToFragmentLocation()
        )
    }

}

//{
//    "resourceType": "Patient",
//    "id": "1904e1c3-4a1f-45da-8dbf-6a95a3515867",
//    "extension": [ {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.registrarState",
//      "valueCoding": {
//        "code": "sikkim",
//        "display": "Sikkim"
//      }
//    }, {
//     "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.registrarDistrict",
//      "valueCoding": {
//        "code": "east-district",
//        "display": "East District"
//      }
//    }, {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.registrarTaluk",
//      "valueCoding": {
//        "code": "chungthang",
//        "display": "Chungthang"
//      }
//    }, {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.registrarStreet",
//      "valueCoding": {
//        "code": "lachung-forest-block",
//        "display": "Lachung Forest Block"
//      }
//    }, {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.abhaGenerationMode",
//      "valueCoding": {
//        "code": "adhar",
//        "display": "Adhar"
//      }
//    }, {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.govtIdType",
//      "valueCoding": {
//        "code": "aadhar",
//        "display": "Aadhar"
//      }
//    }, {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.govtIdNumber",
//      "valueString": "frfr"
//    }, {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.govtHealthProgramType",
//      "valueCoding": {
//        "code": "sanjeevni-vhop",
//        "display": "Sanjeevni VHOP"
//      }
//    }, {
//      "url": "http://hl7.org/fhir/StructureDefinition/Patient#Patient.govtHealthProgramId",
//      "valueString": "89898989898"
//    } ],
//    "name": [ {
//      "family": "last",
//      "given": [ "John Doe" ]
//    } ],
//    "telecom": [ {
//      "system": "phone",
//      "value": "8989898989"
//    } ],
//    "gender": "male",
//    "birthDate": "2023-07-16"
//}