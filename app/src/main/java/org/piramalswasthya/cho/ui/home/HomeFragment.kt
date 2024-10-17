package org.piramalswasthya.cho.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentHomeBinding
import org.piramalswasthya.cho.databinding.FragmentRegisterPatientBinding
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.home_activity.HomeActivityViewModel
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.lang.Exception
import androidx.appcompat.view.menu.MenuBuilder
import android.view.Menu
import android.view.MenuInflater
import androidx.core.content.ContextCompat
import org.piramalswasthya.cho.ui.home_activity.HomeActivity

import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var
            registrarMasterDataRepo: RegistrarMasterDataRepo

    @Inject
    lateinit var preferenceDao: PreferenceDao

    @Inject
    lateinit var malMasterDataRepo: MaleMasterDataRepository
    @Inject
    lateinit var doctorMaleMasterDataRepo: DoctorMasterDataMaleRepo

    @Inject
    lateinit var vaccineAndDoseTypeRepo: VaccineAndDoseTypeRepo

    @Inject
    lateinit var dataLoadFlagManager: DataLoadFlagManager

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!

    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.exit_application))
            .setMessage(resources.getString(R.string.do_you_want_to_exit_application))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                activity?.finish()
            }
            .setNegativeButton(resources.getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
    }


    private lateinit var viewModel: HomeViewModel

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!exitAlert.isShowing)
                    exitAlert.show()

            }
        }
    }

    private val searchPrompt by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.note_ben_reg))
            .setMessage(getString(R.string.please_search_for_beneficiary))
            .setPositiveButton("Search") { dialog, _ ->
                dialog.dismiss()
                HomeViewModel.setSearchBool()
            }
            .setNegativeButton("Proceed with Registration"){dialog, _->
                val intent = Intent(context, RegisterPatientActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
                HomeViewModel.resetSearchBool()
            }
            .create()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setUpWorkerProgress() {
        WorkManager.getInstance(requireContext())
            .getWorkInfosLiveData(WorkQuery.fromUniqueWorkNames(WorkerUtils.syncOneTimeAmritSyncWorker))
            .observe(viewLifecycleOwner) { workInfoMutableList ->
                workInfoMutableList?.let { list ->
                    list.takeIf { it.isNotEmpty() }?.let { workInfoMutableList1 ->
                        workInfoMutableList1.filter { it.state == WorkInfo.State.RUNNING }.takeIf {
                            it.isNotEmpty()
                        }?.first()?.let {
                            WorkerUtils.amritSyncInProgress = true
                            binding.llFullLoadProgress.visibility = View.VISIBLE
                        } ?: run {
                            WorkerUtils.amritSyncInProgress = false
                            binding.llFullLoadProgress.visibility = View.GONE
                        }
                    }
                }
            }

        WorkManager.getInstance(requireContext())
            .getWorkInfosLiveData(WorkQuery.fromUniqueWorkNames(WorkerUtils.syncPeriodicDownSyncWorker))
            .observe(viewLifecycleOwner) { workInfoMutableList ->
                workInfoMutableList?.let { list ->
                    list.takeIf { it.isNotEmpty() }?.let { workInfoMutableList1 ->
                        workInfoMutableList1.filter { it.state == WorkInfo.State.RUNNING }.takeIf {
                            it.isNotEmpty()
                        }?.first()?.let {
                            WorkerUtils.downloadSyncInProgress = true
                        } ?: run {
                            WorkerUtils.downloadSyncInProgress = false
                        }
                    }
                }
            }

        WorkManager.getInstance(requireContext())
            .getWorkInfosLiveData(WorkQuery.fromUniqueWorkNames(WorkerUtils.syncOneTimeDownSyncWorker))
            .observe(viewLifecycleOwner) { workInfoMutableList ->
                workInfoMutableList?.let { list ->
                    list.takeIf { it.isNotEmpty() }?.let { workInfoMutableList1 ->
                        workInfoMutableList1.filter { it.state == WorkInfo.State.RUNNING }.takeIf {
                            it.isNotEmpty()
                        }?.first()?.let {
                            WorkerUtils.downloadSyncInProgress = true
                            binding.llFullLoadProgress.visibility = View.VISIBLE
                        } ?: run {
                            WorkerUtils.downloadSyncInProgress = false
                            binding.llFullLoadProgress.visibility = View.GONE
                        }
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

//        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        super.onViewCreated(view, savedInstanceState)
        val fragmentVisitDetails = PersonalDetailsFragment()

        childFragmentManager.beginTransaction().replace(binding.patientListFragment.id, fragmentVisitDetails).commit()

        if(preferenceDao.isNurseSelected() || preferenceDao.isRegistrarSelected()){
            binding.registration.visibility = View.VISIBLE
            binding.registration.isEnabled = preferenceDao.isNurseSelected() || preferenceDao.isRegistrarSelected()
        }
        else{
            binding.registration.visibility = View.GONE
        }

        WorkerUtils.totalPercentageCompleted.observe(viewLifecycleOwner){
            if(it > 0){
                binding.tvLoadProgress.text = getString(R.string.downloading) + " " + it.toString() + "%"
            }
        }
        binding.registration.bringToFront()
        binding.registration.setOnClickListener {
            searchPrompt.show()

        }

        setUpWorkerProgress()

        HomeActivityViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                HomeActivityViewModel.State.IDLE -> {
                }

                HomeActivityViewModel.State.SAVING -> {
                    if (!dataLoadFlagManager.isDataLoaded()){
                        binding.patientListFragment.visibility = View.GONE
                        binding.rlSaving.visibility = View.VISIBLE
                        binding.registration.isEnabled = false
                    }
                }

                HomeActivityViewModel.State.SAVE_SUCCESS -> {
                    binding.patientListFragment.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                    binding.registration.isEnabled = preferenceDao.isNurseSelected() || preferenceDao.isRegistrarSelected()
                }

                HomeActivityViewModel.State.SAVE_FAILED -> {
                    binding.patientListFragment.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                }
            }
        }
    }


    fun setItemVisibility(){

        if(!preferenceDao.isUserRegistrar() || preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.regis)
        }
        if(!preferenceDao.isUserStaffNurseOrNurse() && !preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.nur)
        }
        if(!preferenceDao.isUserDoctorOrMO() || preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.doc)
        }
        if(!preferenceDao.isUserLabTechnician() && !preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.lab)
        }
        if(!preferenceDao.isUserPharmacist() && !preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.ph)
        }
        if(preferenceDao.isUserCHO()){
            val nurseItem = binding.bottomNavigation.menu.findItem(R.id.nur)
            nurseItem?.title = "CHO"
            val choDrawable = context?.let { ContextCompat.getDrawable(it, R.drawable.cho) }

            // Set the icon using the retrieved Drawable
            nurseItem?.icon = choDrawable
        }

    }

    fun setItemSelected(){

        val registrarItem = binding.bottomNavigation.menu.findItem(R.id.regis)
        val nurseItem = binding.bottomNavigation.menu.findItem(R.id.nur)
        val docItem = binding.bottomNavigation.menu.findItem(R.id.doc)
        val labItem = binding.bottomNavigation.menu.findItem(R.id.lab)
        val phItem = binding.bottomNavigation.menu.findItem(R.id.ph)

        when(preferenceDao.getSwitchRole()){

            "Registrar" -> {
                registrarItem?.isChecked = true
            }
            "Nurse" -> {
                nurseItem?.isChecked = true
            }
            "Doctor" -> {
                docItem?.isChecked = true
            }
            "Lab Technician" -> {
                labItem?.isChecked = true
            }
            "Pharmacist" -> {
                phItem?.isChecked = true
            }
            else -> {
                checkRoleAndSetItem()
            }

        }

    }

    fun checkRoleAndSetItem(){

        val registrarItem = binding.bottomNavigation.menu.findItem(R.id.regis)
        val nurseItem = binding.bottomNavigation.menu.findItem(R.id.nur)
        val docItem = binding.bottomNavigation.menu.findItem(R.id.doc)
        val labItem = binding.bottomNavigation.menu.findItem(R.id.lab)
        val phItem = binding.bottomNavigation.menu.findItem(R.id.ph)

        if(preferenceDao.isUserCHO()){
            nurseItem?.isChecked = true
            preferenceDao.setSwitchRoles("Nurse")
        }
        else if(preferenceDao.isUserRegistrar()){
            registrarItem?.isChecked = true
            preferenceDao.setSwitchRoles("Registrar")
        }
        else if(preferenceDao.isUserStaffNurseOrNurse()){
            nurseItem?.isChecked = true
            preferenceDao.setSwitchRoles("Nurse")
        }
        else if(preferenceDao.isUserDoctorOrMO()){
            docItem?.isChecked = true
            preferenceDao.setSwitchRoles("Doctor")
        }
        else if(preferenceDao.isUserLabTechnician()){
            labItem?.isChecked = true
            preferenceDao.setSwitchRoles("Lab Technician")
        }
        else if(preferenceDao.isUserPharmacist()){
            phItem?.isChecked = true
            preferenceDao.setSwitchRoles("Pharmacist")
        }

        val refresh = Intent(requireContext(), HomeActivity::class.java)
        startActivity(refresh)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.bottom_menu_nav, menu)

        setItemVisibility()
        setItemSelected()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.regis -> {
                    preferenceDao.setSwitchRoles("Registrar")
                    val refresh = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(refresh)
                    true
                }
                R.id.nur -> {
                    preferenceDao.setSwitchRoles("Nurse")
                    val refresh = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(refresh)
                    true
                }
                R.id.doc -> {
                    preferenceDao.setSwitchRoles("Doctor")
                    val refresh = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(refresh)
                    true
                }
                R.id.lab -> {
                    preferenceDao.setSwitchRoles("Lab Technician")
                    val refresh = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(refresh)
                    true
                }
                R.id.ph -> {
                    preferenceDao.setSwitchRoles("Pharmacist")
                    val refresh = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(refresh)
                    true
                }
                else -> {
                    false
                }
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }


}