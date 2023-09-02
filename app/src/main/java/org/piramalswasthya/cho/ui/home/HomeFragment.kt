package org.piramalswasthya.cho.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentHomeBinding
import org.piramalswasthya.cho.databinding.FragmentRegisterPatientBinding
import org.piramalswasthya.cho.repositories.DoctorMasterDataMaleRepo
import org.piramalswasthya.cho.repositories.MaleMasterDataRepository
import org.piramalswasthya.cho.repositories.RegistrarMasterDataRepo
import org.piramalswasthya.cho.repositories.VaccineAndDoseTypeRepo
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsFragment
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.ui.login_activity.username.UsernameFragmentDirections
import org.piramalswasthya.cho.ui.register_patient_activity.RegisterPatientActivity
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var
            registrarMasterDataRepo: RegistrarMasterDataRepo

    @Inject
    lateinit var malMasterDataRepo: MaleMasterDataRepository
    @Inject
    lateinit var doctorMaleMasterDataRepo: DoctorMasterDataMaleRepo

    @Inject
    lateinit var vaccineAndDoseTypeRepo: VaccineAndDoseTypeRepo

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val fragmentVisitDetails = PersonalDetailsFragment()
        childFragmentManager.beginTransaction().replace(binding.patientListFragment.id, fragmentVisitDetails).commit()

        binding.registration.setOnClickListener {
            val intent = Intent(context, RegisterPatientActivity::class.java)
            startActivity(intent)
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRegisterPatientFragment())
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                HomeViewModel.State.IDLE -> {
                }

                HomeViewModel.State.SAVING -> {
                    binding.patientListFragment.visibility = View.GONE
                    binding.rlSaving.visibility = View.VISIBLE
                }

                HomeViewModel.State.SAVE_SUCCESS -> {
                    binding.patientListFragment.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                }

                HomeViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, resources.getString(R.string.something_wend_wong), Toast.LENGTH_LONG
                    ).show()
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
}