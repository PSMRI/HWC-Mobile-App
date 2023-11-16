package org.piramalswasthya.cho.ui.home

import android.content.Intent
import android.os.Bundle
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
                            binding.llFullLoadProgress.visibility = View.VISIBLE
                        } ?: run {
                            binding.llFullLoadProgress.visibility = View.GONE
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
                            binding.llFullLoadProgress.visibility = View.VISIBLE
                        } ?: run {
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

        if((preferenceDao.isUserSwitchRole() && (preferenceDao.getSwitchRole().equals("Registrar") || preferenceDao.getSwitchRole().equals("Nurse"))) ||
            (preferenceDao.isCHO() && (preferenceDao.getCHOSecondRole().equals("Registrar") || preferenceDao.getCHOSecondRole().equals("Nurse")))){
            binding.registration.isEnabled = true
        }
        else binding.registration.isEnabled = !preferenceDao.isCHO() && !preferenceDao.isUserSwitchRole() && (preferenceDao.isUserRegistrar() || preferenceDao.isUserStaffNurseOrNurse())

//

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
//                    binding.registration.isEnabled = preferenceDao.isUserRegistrar()
                }

                HomeActivityViewModel.State.SAVE_FAILED -> {
                    binding.patientListFragment.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                }
            }
        }
//        binding.advanceSearch.setOnClickListener {
//
//        }

//        binding.loginSettings.setOnClickListener{
//            try {
//                findNavController().navigate(
//                    UsernameFragmentDirections.actionUsernameFragmentToLoginSettings(binding.etUsername.text.toString()),
//                )
//            }catch (e: Exception){
//                Timber.d("Failed to navigate"+e.message)
//            }
//
//        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bottom_menu_nav, menu)
        val nurseItem = binding.bottomNavigation.menu.findItem(R.id.nur)
        nurseItem?.isChecked = true
//        if(preferenceDao.isCHO()) {
//            binding.bottomNavigation.getMenu().removeItem(R.id.regis)
////            binding.bottomNavigation.getMenu().removeItem(R.id.nur)
//            binding.bottomNavigation.getMenu().removeItem(R.id.doc)
//            val nurseItem = binding.bottomNavigation.getMenu().findItem(R.id.nur)
//            nurseItem?.setTitle("CHO") // Change title to "CHO"
//            nurseItem?.setIcon(R.drawable.cho)
//        }
        super.onCreateOptionsMenu(menu, inflater)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.your_dynamic_item_id -> {
//                // Handle the click on the dynamically added item
//                // Add your logic here
//                return true
//            }
//            // Handle other menu items as needed
//            // ...
//
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }
}