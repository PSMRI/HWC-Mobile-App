package org.piramalswasthya.cho.ui.commons.fhir_revisit_form

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ResourceType
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.HealthServices
import org.piramalswasthya.cho.adapter.dropdown_adapters.HealthServicesAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.HigherCenterAdapter
import org.piramalswasthya.cho.adapter.dropdown_multiselect.Item
import org.piramalswasthya.cho.databinding.FragmentReferAndRevisitCustomBinding
import org.piramalswasthya.cho.fhir_utils.FhirExtension
import org.piramalswasthya.cho.fhir_utils.extension_names.benFlowID
import org.piramalswasthya.cho.fhir_utils.extension_names.beneficiaryID
import org.piramalswasthya.cho.fhir_utils.extension_names.beneficiaryRegID
import org.piramalswasthya.cho.fhir_utils.extension_names.createdBy
import org.piramalswasthya.cho.fhir_utils.extension_names.parkingPlaceID
import org.piramalswasthya.cho.fhir_utils.extension_names.providerServiceMapId
import org.piramalswasthya.cho.fhir_utils.extension_names.vanID
import org.piramalswasthya.cho.model.ReferRevisitModel
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import java.util.Calendar


/**
 * A simple [Fragment] subclass.
 * Use the [FhirRevisitFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FhirRevisitFormFragment : Fragment(R.layout.fragment_refer_and_revisit_custom), FhirFragmentService, NavigationAdapter {
    private lateinit var higherCenterAdapter: HigherCenterAdapter
    private lateinit var healthServicesAdapter: HealthServicesAdapter

    private var _binding: FragmentReferAndRevisitCustomBinding? = null

    private val binding: FragmentReferAndRevisitCustomBinding
        get() = _binding!!

    override val viewModel: FhirRevisitViewModel by viewModels()
    private val selectedItems = mutableListOf<Item>()
    override var fragment: Fragment = this;
    private var userInfo: UserCache? = null

    override var fragmentContainerId = 0;
    var selectedDate : Int = 0
    var selectedMonth  : Int = 0
    var selectedYear :Int = 0
    var dateRevisit :String?=null

    var appointment = Appointment()
    private val appointmentExtension: FhirExtension = FhirExtension(ResourceType.Appointment)

    var otherHealthServicesValue:String?=null
    var higherCenterValue :String?=null
    var revisitDateValue :String?=null
    var reasonValue :String?=null

    private var isNull:Boolean = true

    override val jsonFile : String = "revisit_form.json"

    private val healthServicesList = listOf(
        HealthServices(1, "104"),
        HealthServices(2, "HIHL"),
        HealthServices(3, "1097 Naco Helpline"),
        HealthServices(4, "ICTC Centre"),
        HealthServices(5, "ART Centre"),
        HealthServices(6, "MCTS/ECD call Centre"),
        HealthServices(7, "ICDS (AWC) Services"),
        HealthServices(8, "Others"),
        // Add more items
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReferAndRevisitCustomBinding.inflate(layoutInflater, container, false)
//        val spinner = binding.multiselectspinner
//        spinner.adapter = adapter
//        val actv = binding.servicesActv
//        val multiSelectAdapter = MultiSelectAdapter(this.requireContext(), R.layout.checkbox_multiselect, items)
//        actv.setAdapter(multiSelectAdapter)

//        val showSelectedButton = binding.showSelectedButton
//        showSelectedButton.setOnClickListener {
//            val selectedItems = items.filter { it.isSelected }
//            val selectedNames = selectedItems.joinToString(", ") { it.name }
////            binding.servicesActv.setText(selectedNames)
//
//            Toast.makeText(this.requireContext(), "Selected items: $selectedNames", Toast.LENGTH_SHORT).show()
//            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner){
            if(it){
                userInfo = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }

        val higherHealthCenterAdapter = ArrayAdapter<String>(requireContext(), R.layout.drop_down)
        binding.higherCenterActv.setAdapter(higherHealthCenterAdapter)

        viewModel.higherHealthCenterDropdown.observe(viewLifecycleOwner) { center ->
            higherHealthCenterAdapter.clear()
            higherHealthCenterAdapter.add("Select None")
            higherHealthCenterAdapter.addAll(center.map { it.institutionName })
            higherHealthCenterAdapter.notifyDataSetChanged()
        }

        //TODO: Single select dropdown for other health services for temporary use, to be replaced with multiselect
        healthServicesAdapter = HealthServicesAdapter(requireContext(), R.layout.drop_down, healthServicesList)
        binding.servicesActv.setAdapter(healthServicesAdapter)

        binding.servicesActv.setOnItemClickListener { parent, _, position, _ ->
            var healthService = parent.getItemAtPosition(position) as HealthServices
            binding.servicesActv.setText(healthService?.name, false)
        }



//        val actv = binding.servicesActv
//        val multiSelectAdapter = MultiSelectAdapter(this.requireContext(), R.layout.checkbox_multiselect, items)
//        actv.setAdapter(multiSelectAdapter)
//        var selectedItems: List<Item>
//        var selectedNames : String?
//        binding.servicesActv.setOnItemClickListener { _, _, _, _->
//             selectedItems = items.filter { it.isSelected }
//             selectedNames = selectedItems.joinToString(", ") { it.name }
//            binding.servicesActv.text = Editable.Factory.getInstance().newEditable(selectedNames)
//        }
//        binding.showSelectedButton.setOnClickListener {
//
//                selectedItems = items.filter { it.isSelected }
//                selectedNames = selectedItems.joinToString(", ") { it.name }
//            if (!selectedNames.isNullOrBlank()) {
//                binding.servicesActv.text =
//                    Editable.Factory.getInstance().newEditable(selectedNames)
//                Toast.makeText(context, "Selected items: $selectedNames", Toast.LENGTH_SHORT).show()
//            }
//        }

        // setting date picker values
        val today = Calendar.getInstance()

        binding.dateEt.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                it.context,
                { _, year, month, day ->
                    binding.dateEt.setText(
                        "${if (day > 9) day else "0$day"}-${if (month > 8) month + 1 else "0${month + 1}"}-$year"
                    )
                    dateRevisit = "$year-${if (month > 8) month + 1 else "0${month + 1}"}-${if (day > 9) day else "0$day"}"
                    selectedDate = day
                    selectedMonth = month+1
                    selectedYear = year
                },
                today.get(Calendar.DAY_OF_MONTH) ,
                today.get(Calendar.MONTH),
                today.get(Calendar.YEAR)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            binding.revisitDateTil.error = null
            if (selectedYear != 0 && selectedMonth != 0 && selectedDate != 0) {
                datePickerDialog.datePicker.updateDate(selectedYear, selectedMonth-1, selectedDate)
            }
            datePickerDialog.show()
        }

    }
    private fun extractFormValues(){
        otherHealthServicesValue = binding.servicesActv.text.toString().trim()
        reasonValue = binding.descInputText.text.toString().trim()
        revisitDateValue = binding.dateEt.text.toString().trim()
        higherCenterValue = binding.higherCenterActv.text.toString().trim()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAppointmentResource(){
        if(!reasonValue.isNullOrBlank()){
            appointment.description = reasonValue
            isNull = false
        }
        if(!otherHealthServicesValue.isNullOrBlank()){
            var serviceCategoryCoding = Coding()
            serviceCategoryCoding.system ="http://example.org/service-category"
            serviceCategoryCoding.code = healthServicesList.find { it.name == otherHealthServicesValue }?.id.toString()
            serviceCategoryCoding.display = otherHealthServicesValue
            val serviceCategoryCodeableConcept  = CodeableConcept()
            serviceCategoryCodeableConcept.addCoding(serviceCategoryCoding)
            appointment.serviceCategory.add(serviceCategoryCodeableConcept)
            isNull = false
        }

        if(!revisitDateValue.isNullOrBlank()) {
            val cal = Calendar.getInstance()
            cal.set(selectedYear, selectedMonth - 1, selectedDate)
            appointment.start = cal.time
            isNull = false
        }

        val actorReferencePatient = Reference()
        actorReferencePatient.reference = "Patient/15252553"
        actorReferencePatient.display = "15252553"
        val patientParticipant = AppointmentParticipantComponent()
        patientParticipant.actor = actorReferencePatient
        patientParticipant.status = Appointment.ParticipationStatus.TENTATIVE
        appointment.participant.add(patientParticipant)

        if(!higherCenterValue.isNullOrBlank() && higherCenterValue!="Select None"){
            val actorReferenceHigherCenter = Reference()
            actorReferenceHigherCenter.reference = "Location/22222222"
            actorReferenceHigherCenter.display = higherCenterValue
            val higherCenterParticipant = AppointmentParticipantComponent()
            higherCenterParticipant.actor = actorReferenceHigherCenter
            higherCenterParticipant.status = Appointment.ParticipationStatus.TENTATIVE
            appointment.participant.add(higherCenterParticipant)
            isNull = false
        }


        if(!isNull) {
            appointment.status = Appointment.AppointmentStatus.BOOKED
            addExtensionsToAppointmentResource(appointment)         //Extensions
        }
    }
    private fun addExtensionsToAppointmentResource(
        appointment: Appointment,
    ) {
        if (userInfo != null) {
            appointment.addExtension(appointmentExtension.getExtenstion(
                appointmentExtension.getUrl(createdBy),
                appointmentExtension.getStringType(userInfo!!.userName)))
            appointment.addExtension( appointmentExtension.getExtenstion(
                appointmentExtension.getUrl(vanID),
                appointmentExtension.getStringType(userInfo!!.vanId.toString()) ) )

            appointment.addExtension( appointmentExtension.getExtenstion(
                appointmentExtension.getUrl(parkingPlaceID),
                appointmentExtension.getStringType(userInfo!!.parkingPlaceId.toString()) ) )

            appointment.addExtension( appointmentExtension.getExtenstion(
                appointmentExtension.getUrl(providerServiceMapId),
                appointmentExtension.getStringType(userInfo!!.serviceMapId.toString()) ) )
        }

       appointment.addExtension(appointmentExtension.getExtenstion(
            appointmentExtension.getUrl(beneficiaryID),
            appointmentExtension.getStringType(""))) //add beneficiaryID

        appointment.addExtension(appointmentExtension.getExtenstion(
            appointmentExtension.getUrl(beneficiaryRegID),
            appointmentExtension.getStringType(""))) //add beneficiaryRegID

        appointment.addExtension(appointmentExtension.getExtenstion(
            appointmentExtension.getUrl(benFlowID),
            appointmentExtension.getStringType(""))) //add benFlowID
    }

    private fun addReferRevisitDataToCache(){
        val referRevisitModel = ReferRevisitModel(
            referId = "1",
            higherCenter =higherCenterValue ,
            referralReason =reasonValue ,
            revisitDate = revisitDateValue,
            otherServices = otherHealthServicesValue,
        )
        viewModel.saveReferInfoToCache(referRevisitModel)
    }
    override fun getFragmentId(): Int {
        return R.id.refer_revisit_custom;
    }

    override fun onSubmitAction() {
//        saveEntity()
//        Log.d("saved", "resource saved appointment")
      navigateNext()
    }

    override fun onCancelAction() {
       findNavController().navigateUp()
    }

    override fun navigateNext() {
//        extractFormValues()
//        addReferRevisitDataToCache()
//        createAppointmentResource()
//        if(!isNull) {
//            viewModel.saveAppointmentResource(appointment)
//            isNull = true
//        }
//        val intent = Intent(context, HomeActivity::class.java)
//        startActivity(intent)
//        findNavController().navigate(
//            FhirRevisitFormFragmentDirections.actionFhirRevisitFormFragmentToCaseRecordCustom()
//        )
    }


}