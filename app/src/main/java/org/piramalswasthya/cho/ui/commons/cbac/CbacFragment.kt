package org.piramalswasthya.cho.ui.commons.cbac

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentCbacBinding
import org.piramalswasthya.cho.model.CbacCache
import org.piramalswasthya.cho.model.Gender
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class CbacFragment : Fragment() , NavigationAdapter  {


    private val binding by lazy { FragmentCbacBinding.inflate(layoutInflater) }

    private val viewModel: CbacViewModel by viewModels()

    private val isInFillMode: Boolean by lazy {
        viewModel.cbacId == 0
    }

    private var totalScorePopupShown: Boolean = false

    private var ed1PopupShown: Boolean = false

    private var ed2PopupShown: Boolean = false

    private val alertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.missing_field)).create()
    }

    private val raAlertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.alert))
//            .setMessage(context?.getString(R.string.ncd_sus_valid))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }
    private val ast1AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.suspected_hrp_and_ncd_case))
            .setMessage(resources.getString(R.string.tb_suspected_alert))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }
    private val ast2AlertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.suspected_hrp_case))
            .setMessage(resources.getString(R.string.tb_suspected_family_alert))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }
    private val ast0AlertDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.alert))
            .setMessage(
                resources.getString(R.string.suspected_ncd_case_please_visit_nearest_hwc_or_call)
            )
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Ran into error! Cbac data not saved!${viewModel.cbacId}")




        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {

                CbacViewModel.State.LOADING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbCbac.visibility = View.VISIBLE
                    Timber.d("IDLE!")
                }

                CbacViewModel.State.IDLE -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbCbac.visibility = View.GONE
                    if (isInFillMode) setUpFill()
                    else setUpView()
                }

                CbacViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbCbac.visibility = View.VISIBLE
                }

                CbacViewModel.State.SAVE_FAIL -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbCbac.visibility = View.GONE
                    Timber.d("Ran into error! Cbac data not saved!")
                    viewModel.resetState()
                }

                CbacViewModel.State.SAVE_SUCCESS -> {
                    Timber.d("CBAC form saved successfully!")
                    viewModel.resetState()
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                    findNavController().navigateUp()
                }

                CbacViewModel.State.MISSING_FIELD -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbCbac.visibility = View.GONE
                    alertDialog.setTitle(resources.getString(R.string.missing_field))
                    alertDialog.setMessage(viewModel.missingFieldString)
                    alertDialog.show()
                }

                else -> {
                    Timber.d("IDLE!")
                }
            }
        }
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }

        viewModel.raAgeScore.observe(viewLifecycleOwner) {
            binding.ddAgeScore.text = it
        }
        viewModel.raSmokeScore.observe(viewLifecycleOwner) {
            binding.ddSmokeScore.text = it
        }
        viewModel.raAlcoholScore.observe(viewLifecycleOwner) {
            binding.ddAlcoholScore.text = it
        }
        viewModel.raWaistScore.observe(viewLifecycleOwner) {
            binding.ddWaistScore.text = it
        }
        viewModel.raPaScore.observe(viewLifecycleOwner) {
            binding.ddPaScore.text = it
        }
        viewModel.raFhScore.observe(viewLifecycleOwner) {
            binding.ddFhScore.text = it
        }
        viewModel.raTotalScore.observe(viewLifecycleOwner) {
            val score = it.substring(it.lastIndexOf(' ') + 1).toInt()
            handleNcdSusBottomInfoDisplay(score)
            handleRaScoreAlert(score)
            binding.cbacTvRaTotalScore.text = it
        }


    }

    private fun handleRaScoreAlert(score: Int) {
        if (
            !binding.actvAgeDropdown.text.isNullOrBlank() &&
            !binding.actvSmokeDropdown.text.isNullOrBlank() &&
            !binding.actvAlcoholDropdown.text.isNullOrBlank() &&
            !binding.actvWaistDropdown.text.isNullOrBlank() &&
            !binding.actvPaDropdown.text.isNullOrBlank() &&
            !binding.actvFhDropdown.text.isNullOrBlank()
        ) {
            if (score > 4) {
                raAlertDialog.setMessage(
                    resources.getString(R.string.refer_to_ncd_screening_day_vhsnd_hwc_for_ncd_screening_priority)
                )
            } else {
                raAlertDialog.setMessage(
                    resources.getString(R.string.refer_to_ncd_screening_day_vhsnd_hwc_for_ncd_screening_less_priority)
                )
            }
            raAlertDialog.show()
        }

    }

    private fun handleAst2Alert() {
        if (
            binding.cbacFhTb.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacTakingTbDrug.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (binding.cbacFhTb.rbYes.isChecked || binding.cbacTakingTbDrug.rbYes.isChecked) {
                ast2AlertDialog.setMessage(
                    resources.getString(R.string.refer_to_mo_or_inform_anm_mpw_to_tracing_of_all_family_members)
                )
                ast2AlertDialog.show()
            }
        }

    }

    private fun handleOldPeopleItemsAlert() {
        if (
            binding.cbacUnsteady.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacPdRm.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacNhop.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacForgetNames.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (
                binding.cbacUnsteady.rbYes.isChecked ||
                binding.cbacPdRm.rbYes.isChecked ||
                binding.cbacNhop.rbYes.isChecked ||
                binding.cbacForgetNames.rbYes.isChecked
            ) {
                alertDialog.setTitle(resources.getString(R.string.alert))
                alertDialog.setMessage(
                    resources.getString(R.string.send_the_patient_to_moic_of_nearest_health_center_for_treatment)
                )
                alertDialog.show()
            }
        }

    }

    private fun handleAst1Alert() {
        if (
            binding.cbacHistb.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacCoughing.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacBlsputum.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacFeverwks.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacLsweight.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacNtswets.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (
                binding.cbacHistb.rbYes.isChecked ||
                binding.cbacCoughing.rbYes.isChecked ||
                binding.cbacBlsputum.rbYes.isChecked ||
                binding.cbacFeverwks.rbYes.isChecked ||
                binding.cbacLsweight.rbYes.isChecked ||
                binding.cbacNtswets.rbYes.isChecked
            ) {
                ast1AlertDialog.setMessage(
                    resources.getString(R.string.refer_to_mo_and_collect_the_sputum_sample)
                )
                ast1AlertDialog.show()
            }
        }

    }

    private fun handleAst0Alert() {
        if (
            binding.cbacHistb.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacCoughing.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacBlsputum.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacFeverwks.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacLsweight.cbacEdRg.checkedRadioButtonId != -1 &&
            binding.cbacNtswets.cbacEdRg.checkedRadioButtonId != -1
        ) {
            if (
                binding.cbacHistb.rbYes.isChecked ||
                binding.cbacCoughing.rbYes.isChecked ||
                binding.cbacBlsputum.rbYes.isChecked ||
                binding.cbacFeverwks.rbYes.isChecked ||
                binding.cbacLsweight.rbYes.isChecked ||
                binding.cbacNtswets.rbYes.isChecked
            ) {
                ast1AlertDialog.setMessage(
                    resources.getString(R.string.refer_to_mo_and_collect_the_sputum_sample)
                )
                ast1AlertDialog.show()
            }
        }

    }

    private fun handleNcdSusBottomInfoDisplay(score: Int) {
        if (score >= 4) {
            binding.ncdSusValidDisplay.visibility = View.VISIBLE
            viewModel.setFlagForNcd(true)
        } else {
            if (binding.ncdSusValidDisplay.visibility != View.GONE) {
                binding.ncdSusValidDisplay.visibility = View.GONE
                viewModel.setFlagForNcd(false)
            }
        }
    }

    fun Long.isOneYearCompleted(): Boolean {
        val oneYearAfter = this + TimeUnit.DAYS.toMillis(365)
        return System.currentTimeMillis() >= oneYearAfter
    }

    private fun setUpView() {

        if (viewModel.lastFillDate.isOneYearCompleted()) {
            binding.btnSave.visibility = View.VISIBLE
            setupDatePicker(
                binding.etDate,
                viewLifecycleOwner,
                viewModel.minDate,
                requireContext()
            )
            binding.btnSave.setOnClickListener {
                viewModel.setFillDate(getLongFromDate(binding.etDate.text.toString()))
                viewModel.submitForm()
            }
            setupRaFill()
            setupEdFill()
            setupRfCopdFill()
            setupPhq2Fill()
            viewModel.filledCbac.observe(viewLifecycleOwner) { cbac ->
                binding.etDate.setText(getDateFromLong(cbac.fillDate))
                setupRaViewForOneYear(cbac)
                setupRfCopdViewforOneYear(cbac)
                setupEdView(cbac)
                setupPhq2ViewforOneyear(cbac)
            }
        } else {
            binding.btnSave.visibility = View.GONE
            viewModel.filledCbac.observe(viewLifecycleOwner) { cbac ->
                binding.etDate.setText(getDateFromLong(cbac.fillDate))
                setupRaView(cbac)
                setupEdView(cbac)
                setupRfCopdView(cbac)
                setupPhq2View(cbac)
            }
        }


    }

    fun getDateFromLong(dateLong: Long): String? {
        if (dateLong == 0L) return null
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateLong
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return f.format(cal.time)


    }

    fun getLongFromDate(dateString: String?): Long {
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val date = dateString?.let { f.parse(it) }
        return date?.time ?: 0L
    }

    private fun setupDatePicker(
        editText: EditText,
        lifecycleOwner: LifecycleOwner,
        minDateLiveData: LiveData<Long>?,
        context: Context
    ) {
        editText.setText(getDateFromLong(System.currentTimeMillis()))

        editText.setOnClickListener {
            val today = Calendar.getInstance()
            val thisYear = today.get(Calendar.YEAR)
            val thisMonth = today.get(Calendar.MONTH)
            val thisDay = today.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, day ->
                    val selectedDate = String.format("%02d-%02d-%d", day, month + 1, year)
                    editText.setText(selectedDate)
                },
                thisYear,
                thisMonth,
                thisDay
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            minDateLiveData?.observe(lifecycleOwner) { minDate ->
                datePickerDialog.datePicker.minDate = minDate
            }

            datePickerDialog.show()
        }
    }

    private fun setUpFill() {
        binding.btnSave.setOnClickListener {
            viewModel.setFillDate(getLongFromDate(binding.etDate.text.toString()))
            viewModel.submitForm()
        }
        setupDatePicker(
            binding.etDate,
            viewLifecycleOwner,
            viewModel.minDate,
            requireContext()
        )

        setupRaFill()
        setupEdFill()
        setupRfCopdFill()
        setupPhq2Fill()
    }

    private fun setupRaFill() {

        viewModel.gender.observe(viewLifecycleOwner) {
            when (it) {
                Gender.MALE -> {
                    binding.actvWaistDropdown.setSimpleItems(R.array.cbac_waist_mes_male)
                    binding.cbacLlEdWomen.visibility = View.GONE
                }

                else -> binding.actvWaistDropdown.setSimpleItems(R.array.cbac_waist_mes_female)
            }
        }
        viewModel.raAgeText.observe(viewLifecycleOwner) {
            binding.actvAgeDropdown.setText(it)
        }
        binding.actvSmokeDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                R.id.tv_dropdown_item_text,
                resources.getStringArray(R.array.cbac_smoke),


                )
        )
        binding.actvPaDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                R.id.tv_dropdown_item_text,
                resources.getStringArray(R.array.cbac_pa),


                )
        )
        binding.actvSmokeDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setSmoke(i)
        }
        binding.actvAlcoholDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setAlcohol(i)
        }
        binding.actvWaistDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setWaist(i)
        }
        binding.actvPaDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setPa(i)
        }
        binding.actvFhDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setFh(i)
        }
    }
   private fun setupRaViewForOneYear(cbac: CbacCache) {
       val ageOptions = resources.getStringArray(R.array.cbac_age)
       val smokeOptions = resources.getStringArray(R.array.cbac_smoke)
       val alcoholOptions = resources.getStringArray(R.array.cbac_alcohol)
       val paOptions = resources.getStringArray(R.array.cbac_pa)
       val fhOptions = resources.getStringArray(R.array.cbac_fh)

       binding.actvSmokeDropdown.setAdapter(
           ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, smokeOptions)
       )
       binding.actvAlcoholDropdown.setAdapter(
           ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, alcoholOptions)
       )
       binding.actvPaDropdown.setAdapter(
           ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paOptions)
       )
       binding.actvFhDropdown.setAdapter(
           ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fhOptions)
       )

       binding.actvSmokeDropdown.setText(smokeOptions[cbac.cbac_smoke_posi - 1], false)
       binding.actvAlcoholDropdown.setText(alcoholOptions[cbac.cbac_alcohol_posi - 1], false)
       binding.actvPaDropdown.setText(paOptions[cbac.cbac_pa_posi - 1], false)
       binding.actvFhDropdown.setText(fhOptions[cbac.cbac_familyhistory_posi - 1], false)

       viewModel.gender.observe(viewLifecycleOwner) {
           if (it == Gender.MALE) {
               val maleWaistOptions = resources.getStringArray(R.array.cbac_waist_mes_male)
               binding.actvWaistDropdown.setAdapter(
                   ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, maleWaistOptions)
               )
               binding.actvWaistDropdown.setText(maleWaistOptions[cbac.cbac_waist_posi - 1], false)
               binding.cbacLlEdWomen.visibility = View.GONE
           } else {
               val femaleWaistOptions = resources.getStringArray(R.array.cbac_waist_mes_female)
               binding.actvWaistDropdown.setAdapter(
                   ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, femaleWaistOptions)
               )
               binding.actvWaistDropdown.setText(femaleWaistOptions[cbac.cbac_waist_posi - 1], false)
           }
       }
       binding.actvAgeDropdown.setText(resources.getStringArray(R.array.cbac_age)[cbac.cbac_age_posi - 1])
   }

    private fun setupRaView(cbac: CbacCache) {

        try {
            binding.actvAgeDropdown.setText(resources.getStringArray(R.array.cbac_age)[cbac.cbac_age_posi - 1])
            binding.actvSmokeDropdown.setText(resources.getStringArray(R.array.cbac_smoke)[cbac.cbac_smoke_posi - 1])
            binding.actvAlcoholDropdown.setText(resources.getStringArray(R.array.cbac_alcohol)[cbac.cbac_alcohol_posi - 1])
            viewModel.gender.observe(viewLifecycleOwner) {
                when (it) {
                    Gender.MALE -> {
                        binding.actvWaistDropdown.setText(resources.getStringArray(R.array.cbac_waist_mes_male)[cbac.cbac_waist_posi - 1])
                        binding.cbacLlEdWomen.visibility = View.GONE
                    }

                    else -> binding.actvWaistDropdown.setText(resources.getStringArray(R.array.cbac_waist_mes_female)[cbac.cbac_waist_posi - 1])

                }
            }

            binding.actvPaDropdown.setText(resources.getStringArray(R.array.cbac_pa)[cbac.cbac_pa_posi - 1])
            binding.actvFhDropdown.setText(resources.getStringArray(R.array.cbac_fh)[cbac.cbac_familyhistory_posi - 1])

            viewModel.setSmoke(cbac.cbac_smoke_posi - 1)
            viewModel.setAlcohol(cbac.cbac_alcohol_posi - 1)
            viewModel.setWaist(cbac.cbac_waist_posi - 1)
            viewModel.setPa(cbac.cbac_pa_posi - 1)
            viewModel.setFh(cbac.cbac_familyhistory_posi - 1)
        } catch (e:Exception) {
            Timber.e("Exception  $e")
        }

    }

    private fun setupEdFill() {

        viewModel.ast1.observe(viewLifecycleOwner) {
            Timber.d("value of ast1 : $it")
            if (it > 0) {
                binding.tvTbSputumCollect.visibility = View.VISIBLE
                viewModel.setCollectSputum(1)
            } else {
                binding.tvTbSputumCollect.visibility = View.GONE
                viewModel.setCollectSputum(1)
            }
        }
        viewModel.ast2.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.tbSusValidDisplay.visibility = View.VISIBLE
                viewModel.setTraceAllMembers(1)
            } else {
                binding.tbSusValidDisplay.visibility = View.GONE
                viewModel.setTraceAllMembers(0)
            }
        }
        viewModel.astMoic.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.tvTbMoicVisit.visibility = View.VISIBLE
                viewModel.setReferMoic(1)
            } else {
                binding.tvTbMoicVisit.visibility = View.GONE
                viewModel.setReferMoic(0)
            }
        }
        binding.cbacFhTb.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setFhTb(1)
                R.id.rb_no -> viewModel.setFhTb(2)
            }
            handleAst2Alert()
        }
        binding.cbacTakingTbDrug.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setTakingTbDrug(1)
                R.id.rb_no -> viewModel.setTakingTbDrug(2)
            }
            handleAst2Alert()
        }
        binding.cbacHistb.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHisTb(1)
                R.id.rb_no -> viewModel.setHisTb(2)
            }
            handleAst1Alert()
        }
        binding.cbacCoughing.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setCoughing(1)
                R.id.rb_no -> viewModel.setCoughing(2)
            }
            handleAst1Alert()
        }
        binding.cbacBlsputum.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setBloodSputum(1)
                R.id.rb_no -> viewModel.setBloodSputum(2)
            }
            handleAst1Alert()
        }
        binding.cbacFeverwks.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setFeverWks(1)
                R.id.rb_no -> viewModel.setFeverWks(2)
            }
            handleAst1Alert()
        }
        binding.cbacLsweight.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setLsWt(1)
                R.id.rb_no -> viewModel.setLsWt(2)
            }
            handleAst1Alert()
        }
        binding.cbacNtswets.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNtSwets(1)
                R.id.rb_no -> viewModel.setNtSwets(2)
            }
            handleAst1Alert()

        }
        binding.cbacRecurrentUlceration.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setRecurrentUlceration(1)
                R.id.rb_no -> viewModel.setRecurrentUlceration(2)
            }
        }
        binding.cbacRecurrentCloudy.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setRecurrentCloudy(1)
                R.id.rb_no -> viewModel.setRecurrentCloudy(2)
            }
        }
        binding.cbacRecurrentDiffcultyReading.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setDiffReading(1)
                R.id.rb_no -> viewModel.setDiffReading(2)
            }
        }

        binding.cbacRecurrentPainEyes.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setPainEyes(1)
                R.id.rb_no -> viewModel.setPainEyes(2)
            }
        }
        binding.cbacRecurrentRednessEyes.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setRedEyes(1)
                R.id.rb_no -> viewModel.setRedEyes(2)
            }
        }
        binding.cbacRecurrentDiffHearing.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setDiffHearing(1)
                R.id.rb_no -> viewModel.setDiffHearing(2)
            }
        }
        binding.cbacBreath.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setBreathe(1)
                R.id.rb_no -> viewModel.setBreathe(2)
            }
        }
        binding.cbacRecurrentTingling.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setTing(1)
                R.id.rb_no -> viewModel.setTing(2)
            }
        }
        binding.cbacHifits.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHisFits(1)
                R.id.rb_no -> viewModel.setHisFits(2)
            }
        }
        binding.cbacDifmouth.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setDiffMouth(1)
                R.id.rb_no -> viewModel.setDiffMouth(2)
            }
        }
        binding.cbacHeald.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHealed(1)
                R.id.rb_no -> viewModel.setHealed(2)
            }
        }
        binding.cbacVoice.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setVoice(1)
                R.id.rb_no -> viewModel.setVoice(2)
            }
        }
        binding.cbacAnyGrowth.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setAnyGrowth(1)
                R.id.rb_no -> viewModel.setAnyGrowth(2)
            }
        }
        binding.cbacAnyWhite.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setAnyWhite(1)
                R.id.rb_no -> viewModel.setAnyWhite(2)
            }
        }
        binding.cbacPainWhileChewing.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setPainChew(1)
                R.id.rb_no -> viewModel.setPainChew(2)
            }
        }
        binding.cbacAnyHyperPigmented.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHyperPig(1)
                R.id.rb_no -> viewModel.setHyperPig(2)
            }
        }
        binding.cbacAnyThickendSkin.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setThickSkin(1)
                R.id.rb_no -> viewModel.setThickSkin(2)
            }
        }
        binding.cbacAnyNodulesSkin.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNoduleSkin(1)
                R.id.rb_no -> viewModel.setNoduleSkin(2)
            }
        }
        binding.cbacRecurrentNumbness.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNumb(1)
                R.id.rb_no -> viewModel.setNumb(2)
            }
        }
        binding.cbacClawingOfFingers.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setClaw(1)
                R.id.rb_no -> viewModel.setClaw(2)
            }
        }
        binding.cbacTinglingOrNumbness.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setTingNumb(1)
                R.id.rb_no -> viewModel.setTingNumb(2)
            }
        }
        binding.cbacInabilityCloseEyelid.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setCloseEyelid(1)
                R.id.rb_no -> viewModel.setCloseEyelid(2)
            }
        }
        binding.cbacDiffHoldingObjects.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setHoldObj(1)
                R.id.rb_no -> viewModel.setHoldObj(2)
            }
        }
        binding.cbacWeeknessInFeet.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setWeakFeet(1)
                R.id.rb_no -> viewModel.setWeakFeet(2)
            }
        }
        binding.cbacLumpbrest.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setLumpB(1)
                R.id.rb_no -> viewModel.setLumpB(2)
            }
        }
        binding.cbacNipple.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNipple(1)
                R.id.rb_no -> viewModel.setNipple(2)
            }
        }
        binding.cbacBreast.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setBreast(1)
                R.id.rb_no -> viewModel.setBreast(2)
            }
        }
        binding.cbacBlperiods.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setBlP(1)
                R.id.rb_no -> viewModel.setBlP(2)
            }
        }
        binding.cbacBlmenopause.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> {
                    viewModel.setBlM(1)
                    alertDialog.setTitle(resources.getString(R.string.alert))
                    alertDialog.setMessage(resources.getString(R.string.inform_asha_facilitator))
                    alertDialog.show()
                    binding.tvBlMenopause.visibility = View.VISIBLE
                }

                R.id.rb_no -> {
                    viewModel.setBlM(2)
                    binding.tvBlMenopause.visibility = View.GONE
                }
            }
        }
        binding.cbacBlintercorse.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setBlI(1)
                R.id.rb_no -> viewModel.setBlI(2)
            }
        }
        binding.cbacFouldis.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setFoulD(1)
                R.id.rb_no -> viewModel.setFoulD(2)
            }
        }
        viewModel.age.observe(viewLifecycleOwner) {
            if (it >= 60) binding.cbacLlEdElderly.visibility = View.VISIBLE
            else if (binding.cbacLlEdElderly.visibility != View.GONE) binding.cbacLlEdElderly.visibility =
                View.GONE
        }
        binding.cbacUnsteady.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setUnsteady(1)
                R.id.rb_no -> viewModel.setUnsteady(2)
            }
            handleOldPeopleItemsAlert()
        }
        binding.cbacPdRm.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setPdRm(1)
                R.id.rb_no -> viewModel.setPdRm(2)
            }
            handleOldPeopleItemsAlert()
        }
        binding.cbacNhop.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setNhop(1)
                R.id.rb_no -> viewModel.setNhop(2)
            }
            handleOldPeopleItemsAlert()
        }
        binding.cbacForgetNames.cbacEdRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_yes -> viewModel.setForgetNames(1)
                R.id.rb_no -> viewModel.setForgetNames(2)
            }
            handleOldPeopleItemsAlert()
        }
        //ED END
    }

    private fun setSelectedForRg(rg: RadioGroup, pos: Int) {
        pos.takeIf { it > 0 }?.let {
            rg.check(
                when (it) {
                    1 -> R.id.rb_yes
                    2 -> R.id.rb_no
                    else -> throw IllegalArgumentException("LALALLALALALALLALALALA")
                }
            )
        }

        if (viewModel.lastFillDate.isOneYearCompleted()) {
            rg.children.forEach {
                it.isClickable = true
            }
        } else {
            rg.children.forEach {
                it.isClickable = isInFillMode
            }
        }
    }

    private fun setupEdView(cbac: CbacCache) {
        setSelectedForRg(binding.cbacFhTb.cbacEdRg, cbac.cbac_sufferingtb_pos)
        setSelectedForRg(binding.cbacTakingTbDrug.cbacEdRg, cbac.cbac_antitbdrugs_pos)
        setSelectedForRg(binding.cbacHistb.cbacEdRg, cbac.cbac_tbhistory_pos)
        setSelectedForRg(binding.cbacCoughing.cbacEdRg, cbac.cbac_coughing_pos)
        setSelectedForRg(binding.cbacBlsputum.cbacEdRg, cbac.cbac_bloodsputum_pos)
        setSelectedForRg(binding.cbacFeverwks.cbacEdRg, cbac.cbac_fivermore_pos)
        setSelectedForRg(binding.cbacLsweight.cbacEdRg, cbac.cbac_loseofweight_pos)
        setSelectedForRg(binding.cbacNtswets.cbacEdRg, cbac.cbac_nightsweats_pos)
        setSelectedForRg(binding.cbacRecurrentUlceration.cbacEdRg, cbac.cbac_uicers_pos)
        setSelectedForRg(binding.cbacRecurrentCloudy.cbacEdRg, cbac.cbac_cloudy_posi)
        setSelectedForRg(binding.cbacRecurrentDiffcultyReading.cbacEdRg, cbac.cbac_diffreading_posi)
        setSelectedForRg(binding.cbacRecurrentPainEyes.cbacEdRg, cbac.cbac_pain_ineyes_posi)
        setSelectedForRg(binding.cbacRecurrentRednessEyes.cbacEdRg, cbac.cbac_redness_ineyes_posi)
        setSelectedForRg(binding.cbacRecurrentDiffHearing.cbacEdRg, cbac.cbac_diff_inhearing_posi)
        setSelectedForRg(binding.cbacBreath.cbacEdRg, cbac.cbac_sortnesofbirth_pos)
        setSelectedForRg(binding.cbacRecurrentTingling.cbacEdRg, cbac.cbac_tingling_palm_posi)
        setSelectedForRg(binding.cbacHifits.cbacEdRg, cbac.cbac_historyoffits_pos)
        setSelectedForRg(binding.cbacDifmouth.cbacEdRg, cbac.cbac_difficultyinmouth_pos)
        setSelectedForRg(binding.cbacHeald.cbacEdRg, cbac.cbac_growth_in_mouth_posi)
        setSelectedForRg(binding.cbacVoice.cbacEdRg, cbac.cbac_toneofvoice_pos)
        setSelectedForRg(binding.cbacAnyGrowth.cbacEdRg, cbac.cbac_growth_in_mouth_posi)
        setSelectedForRg(binding.cbacAnyWhite.cbacEdRg, cbac.cbac_white_or_red_patch_posi)
        setSelectedForRg(binding.cbacPainWhileChewing.cbacEdRg, cbac.cbac_Pain_while_chewing_posi)
        setSelectedForRg(
            binding.cbacAnyHyperPigmented.cbacEdRg, cbac.cbac_hyper_pigmented_patch_posi
        )
        setSelectedForRg(binding.cbacAnyThickendSkin.cbacEdRg, cbac.cbac_any_thickend_skin_posi)
        setSelectedForRg(binding.cbacAnyNodulesSkin.cbacEdRg, cbac.cbac_nodules_on_skin_posi)
        setSelectedForRg(
            binding.cbacRecurrentNumbness.cbacEdRg, cbac.cbac_tingling_or_numbness_posi
        )
        setSelectedForRg(binding.cbacClawingOfFingers.cbacEdRg, cbac.cbac_clawing_of_fingers_posi)
        setSelectedForRg(
            binding.cbacTinglingOrNumbness.cbacEdRg, cbac.cbac_tingling_or_numbness_posi
        )
        setSelectedForRg(
            binding.cbacInabilityCloseEyelid.cbacEdRg, cbac.cbac_inability_close_eyelid_posi
        )
        setSelectedForRg(binding.cbacDiffHoldingObjects.cbacEdRg, cbac.cbac_diff_holding_obj_posi)
        setSelectedForRg(binding.cbacWeeknessInFeet.cbacEdRg, cbac.cbac_weekness_in_feet_posi)
        setSelectedForRg(binding.cbacLumpbrest.cbacEdRg, cbac.cbac_lumpinbreast_pos)
        setSelectedForRg(binding.cbacNipple.cbacEdRg, cbac.cbac_blooddischage_pos)
        setSelectedForRg(binding.cbacBreast.cbacEdRg, cbac.cbac_changeinbreast_pos)
        setSelectedForRg(binding.cbacBlperiods.cbacEdRg, cbac.cbac_bleedingbtwnperiods_pos)
        setSelectedForRg(
            binding.cbacBlmenopause.cbacEdRg, cbac.cbac_bleedingaftermenopause_pos
        ).also {
            binding.tvBlMenopause.visibility =
                if (cbac.cbac_bleedingaftermenopause_pos == 1) View.VISIBLE else View.GONE
        }
        setSelectedForRg(binding.cbacBlintercorse.cbacEdRg, cbac.cbac_bleedingafterintercourse_pos)
        setSelectedForRg(binding.cbacFouldis.cbacEdRg, cbac.cbac_foulveginaldischarge_pos)
        viewModel.age.observe(viewLifecycleOwner) {
            if (it >= 60) binding.cbacLlEdElderly.visibility = View.VISIBLE
            else if (binding.cbacLlEdElderly.visibility != View.GONE) binding.cbacLlEdElderly.visibility =
                View.GONE
        }
        setSelectedForRg(binding.cbacUnsteady.cbacEdRg, cbac.cbac_feeling_unsteady_posi)
        setSelectedForRg(binding.cbacPdRm.cbacEdRg, cbac.cbac_suffer_physical_disability_posi)
        setSelectedForRg(binding.cbacNhop.cbacEdRg, cbac.cbac_needing_help_posi)
        setSelectedForRg(binding.cbacForgetNames.cbacEdRg, cbac.cbac_forgetting_names_posi)
    }


    private fun setupRfCopdFill() {
        binding.actvExposureDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                R.id.tv_dropdown_item_text,
                resources.getStringArray(R.array.cbac_type_occupational_exposure),


                )
        )
        binding.actvFuelDropdown.setOnItemClickListener { _, _, i, _ -> viewModel.setFuelType(i) }
        binding.actvExposureDropdown.setOnItemClickListener { _, _, i, _ ->
            viewModel.setOccExposure(i)
        }
    }
    private fun setupRfCopdViewforOneYear(cbac: CbacCache) {
        val fuelOptions = resources.getStringArray(R.array.cbac_type_Cooking_fuel)
        binding.actvFuelDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fuelOptions)
        )
        cbac.cbac_fuel_used_posi.takeIf { it > 0 }?.let {
            binding.actvFuelDropdown.setText(fuelOptions[it - 1], false)
        }
        val exposureOptions = resources.getStringArray(R.array.cbac_type_occupational_exposure)
        binding.actvExposureDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, exposureOptions)
        )
        cbac.cbac_occupational_exposure_posi.takeIf { it > 0 }?.let {
            binding.actvExposureDropdown.setText(exposureOptions[it - 1], false)
        }
    }

    private fun setupRfCopdView(cbac: CbacCache) {
        cbac.cbac_fuel_used_posi.takeIf { it > 0 }?.let {
            binding.actvFuelDropdown.setText(resources.getStringArray(R.array.cbac_type_Cooking_fuel)[it - 1])
        }
        cbac.cbac_occupational_exposure_posi.takeIf { it > 0 }?.let {
            binding.actvExposureDropdown.setText(resources.getStringArray(R.array.cbac_type_occupational_exposure)[it - 1])
        }

    }


    private fun setupPhq2Fill() {
        //PHQ2 START
        binding.actvLiDropdown.setOnItemClickListener { _, _, i, _ -> viewModel.setLi(i) }
        binding.actvFdDropdown.setOnItemClickListener { _, _, i, _ -> viewModel.setFd(i) }
        viewModel.phq2LittleInterestScore.observe(viewLifecycleOwner) {
            binding.ddLiScore.text = it
        }
        viewModel.phq2FeelDownDepScore.observe(viewLifecycleOwner) {
            binding.ddFdScore.text = it
        }
        viewModel.phq2TotalScore.observe(viewLifecycleOwner) {
            if (it.substring(it.lastIndexOf(' ') + 1).toInt() > 3) {
                binding.tvTbMoicVisit.visibility = View.VISIBLE
                viewModel.setFlagForPhQ2(true)
            } else {
                if (binding.tvTbMoicVisit.visibility != View.GONE) {
                    binding.tvTbMoicVisit.visibility = View.GONE
                    viewModel.setFlagForPhQ2(false)
                }
            }
            binding.cbacPhq2TotalScore.text = it
        }
    }

    private fun setupPhq2ViewforOneyear(cbac: CbacCache) {
        val liOptions = resources.getStringArray(R.array.cbac_li)
        binding.actvLiDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, liOptions)
        )
        cbac.cbac_little_interest_posi.takeIf { it > 0 }?.let {
            binding.actvLiDropdown.setText(liOptions[it - 1], false)
        }

        val fdOptions = resources.getStringArray(R.array.cbac_fd)
        binding.actvFdDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fdOptions)
        )
        cbac.cbac_feeling_down_posi.takeIf { it > 0 }?.let {
            binding.actvFdDropdown.setText(fdOptions[it - 1], false)
        }

        binding.ddLiScore.text = cbac.cbac_little_interest_score.toString()
        binding.ddFdScore.text = cbac.cbac_feeling_down_score.toString()
        binding.cbacPhq2TotalScore.text = getString(
            R.string.total_score_wihout_semi_colon
        ) + ": " + (cbac.cbac_little_interest_score + cbac.cbac_feeling_down_score)
    }

    private fun setupPhq2View(cbac: CbacCache) {
        cbac.cbac_little_interest_posi.takeIf { it > 0 }?.let {
            binding.actvLiDropdown.setText(resources.getStringArray(R.array.cbac_li)[it - 1])
        }
        cbac.cbac_feeling_down_posi.takeIf { it > 0 }?.let {
            binding.actvFdDropdown.setText(resources.getStringArray(R.array.cbac_fd)[it - 1])
        }
        binding.ddLiScore.text = cbac.cbac_little_interest_score.toString()
        binding.ddFdScore.text = cbac.cbac_feeling_down_score.toString()
        binding.cbacPhq2TotalScore.text =
            String.format(
                "%s%s%s", resources.getString(R.string.total_score_wihout_semi_colon),
                ": ", cbac.cbac_little_interest_score + cbac.cbac_feeling_down_score
            )
    }

    fun navigateNext() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun getFragmentId(): Int {
        return R.id.cbacFragment
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }

    override fun onStart() {
        super.onStart()
//        activity?.let {
//            (it as HomeActivity).updateActionBar(
//                R.drawable.ic__ncd_eligibility,
//                getString(R.string.cbac)
//            )
//        }
    }

}