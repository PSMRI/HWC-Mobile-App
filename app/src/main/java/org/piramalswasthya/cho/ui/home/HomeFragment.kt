package org.piramalswasthya.cho.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentHomeBinding
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivityViewModel
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
import org.piramalswasthya.cho.work.WorkerUtils
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
                (requireActivity().application as CHOApplication).closeAllActivities()
                System.exit(0)
            }
            .setNegativeButton(resources.getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
    }


    private val activityViewModel by activityViewModels<HomeActivityViewModel>()
    private var lastObservedRole: String? = null

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
            .setPositiveButton(getString(R.string.search)) { dialog, _ ->
                dialog.dismiss()
                HomeViewModel.setSearchBool()
            }
            .setNegativeButton(getString(R.string.proceed_with_registration)) { dialog, _ ->
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
        activityViewModel.currentRole.observe(viewLifecycleOwner) { role ->
            if (role != lastObservedRole) {
                lastObservedRole = role
                childFragmentManager.beginTransaction()
                    .replace(binding.patientListFragment.id, PersonalDetailsFragment())
                    .commit()
            }
            val showReg = preferenceDao.isNurseSelected() || preferenceDao.isRegistrarSelected()
            binding.registration.visibility = if (showReg) View.VISIBLE else View.GONE
            binding.registration.isEnabled = showReg
        }

        WorkerUtils.totalPercentageCompleted.observe(viewLifecycleOwner){
            if(it > 0){
                binding.tvLoadProgress.text = getString(R.string.downloading) + " " + it.toString() + "%"
                binding.pbLoadProgress.progress = it
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

//        if(!preferenceDao.isUserRegistrar() || preferenceDao.isUserCHO()){
//            binding.bottomNavigation.menu.removeItem(R.id.nav_registrar)
//        }
        if(!preferenceDao.isUserStaffNurseOrNurse() && !preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.nav_nurse)
        }
        if(!preferenceDao.isUserDoctorOrMO() || preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.nav_doctor)
        }
        if(!preferenceDao.isUserLabTechnician() && !preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.nav_lab_technician)
        }
        if(!preferenceDao.isUserPharmacist() && !preferenceDao.isUserCHO()){
            binding.bottomNavigation.menu.removeItem(R.id.nav_pharmacist)
        }
        if(preferenceDao.isUserCHO() || preferenceDao.isUserRegistrar()){
            val nurseItem = binding.bottomNavigation.menu.findItem(R.id.nav_nurse)
            nurseItem?.title = getString(R.string.cho_role)
            val choDrawable = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_medical_briefcase) } // R.drawable.cho

            // Set the icon using the retrieved Drawable
            nurseItem?.icon = choDrawable
        }

    }

    fun setItemSelected(){

//        val registrarItem = binding.bottomNavigation.menu.findItem(R.id.nav_registrar)
        val nurseItem = binding.bottomNavigation.menu.findItem(R.id.nav_nurse)
        val docItem = binding.bottomNavigation.menu.findItem(R.id.nav_doctor)
        val labItem = binding.bottomNavigation.menu.findItem(R.id.nav_lab_technician)
        val phItem = binding.bottomNavigation.menu.findItem(R.id.nav_pharmacist)

        when(preferenceDao.getSwitchRole()){

//            "Registrar" -> {
//                registrarItem?.isChecked = true
//            }
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

//        val registrarItem = binding.bottomNavigation.menu.findItem(R.id.nav_registrar)
        val nurseItem = binding.bottomNavigation.menu.findItem(R.id.nav_nurse)
        val docItem = binding.bottomNavigation.menu.findItem(R.id.nav_doctor)
        val labItem = binding.bottomNavigation.menu.findItem(R.id.nav_lab_technician)
        val phItem = binding.bottomNavigation.menu.findItem(R.id.nav_pharmacist)

        if(preferenceDao.isUserCHO()){
            nurseItem?.isChecked = true
            activityViewModel.switchRole("Nurse")
        }
//        else if(preferenceDao.isUserRegistrar()){
//            registrarItem?.isChecked = true
//            activityViewModel.switchRole("Registrar")
//        }
        else if(preferenceDao.isUserStaffNurseOrNurse()){
            nurseItem?.isChecked = true
            activityViewModel.switchRole("Nurse")
        }
        else if(preferenceDao.isUserDoctorOrMO()){
            docItem?.isChecked = true
            activityViewModel.switchRole("Doctor")
        }
        else if(preferenceDao.isUserLabTechnician()){
            labItem?.isChecked = true
            activityViewModel.switchRole("Lab Technician")
        }
        else if(preferenceDao.isUserPharmacist()){
            phItem?.isChecked = true
            activityViewModel.switchRole("Pharmacist")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.bottom_menu_nav, menu)

        setItemVisibility()
        setItemSelected()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.nav_registrar -> {
//                    activityViewModel.switchRole("Registrar")
//                    true
//                }
                R.id.nav_nurse -> {
                    activityViewModel.switchRole("Nurse")
                    true
                }
                R.id.nav_doctor -> {
                    activityViewModel.switchRole("Doctor")
                    true
                }
                R.id.nav_lab_technician -> {
                    activityViewModel.switchRole("Lab Technician")
                    true
                }
                R.id.nav_pharmacist -> {
                    activityViewModel.switchRole("Pharmacist")
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