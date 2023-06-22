package org.piramalswasthya.cho.ui.edit_patient_details_activity.vitals_form
//
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentPrescriptionBinding
import org.piramalswasthya.cho.databinding.FragmentVitalsFormBinding
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.edit_patient_details_activity.prescription.PrescriptionFragment

class VitalsFormFragment constructor(
    private val patientDetails: PatientDetails,
): Fragment() {

    private var _binding: FragmentVitalsFormBinding? = null

    private val binding: FragmentVitalsFormBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVitalsFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

//    companion object {
//        fun newInstance(): VitalsFormFragment {
//            return VitalsFormFragment()
//        }
//    }

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_vitals_form, container, false)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submitButton.setOnClickListener {
                findNavController().navigate(R.id.action_vitalsFormFragment_to_prescriptionFragment)


//
//
//                val patient = PatientDetails(
//                    weight = binding.weightEditText.text.toString().toFloat(),
//                    temperature = binding.temperatureEditText.text.toString().toFloat(),
//                    bloodPressureDiastolic = binding.bpDiastolicEditText.text.toString().toFloat(),
//                    bloodPressureSystolic = binding.bpSystolicEditText.text.toString().toFloat()
//                )



        }
    }
}