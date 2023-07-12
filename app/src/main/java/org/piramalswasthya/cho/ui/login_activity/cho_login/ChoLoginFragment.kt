package org.piramalswasthya.cho.ui.login_activity.cho_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.ui.login_activity.cho_login.hwc.HwcFragment
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachFragment
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ChoLoginFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository
    private var loginSettingsData: LoginSettingsData? = null
    private var _binding: FragmentChoLoginBinding? = null

    private val binding: FragmentChoLoginBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChoLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    fun setActivityContainer(programId: Int){
        val userName = (arguments?.getString("userName", ""))!!;
        val rememberUsername:Boolean = (arguments?.getBoolean("rememberUsername"))!!
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        val hwcFragment  = HwcFragment(userName)
        val outreachFragment = OutreachFragment(userName,rememberUsername);
        when (programId){
            binding.btnHwc.id -> {
                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
                fragmentTransaction.commit()
            }
            binding.btnOutreach.id -> {
                fragmentTransaction.replace(binding.selectActivityContainer.id, outreachFragment)
                fragmentTransaction.commit()
            }
            else -> {
                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
                fragmentTransaction.commit()
            }
        }
    }

//    fun setUserName(){
//        userName = arguments?.getString("userName", "");
//        if(userName != null) {
//            Timber.tag("tag").i(userName)
//        }
//        else{
//            Timber.tag("tag").i("null")
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setActivityContainer(binding.selectProgram.id)
        binding.selectProgram.setOnCheckedChangeListener { _, programId ->
            setActivityContainer(programId)
        }
//        val userName = (arguments?.getString("userName", ""))!!;
//
//        lifecycleScope.launch {
//            loginSettingsData =  loginSettingsDataRepository.getLoginSettingsDataByUsername(userName)
//
//            if (loginSettingsData==null) {
//                binding.loginSettings.visibility = View.VISIBLE
//
//                binding.loginSettings.setOnClickListener{
//                    try {
//                        findNavController().navigate(
//                            ChoLoginFragmentDirections.actionChoLoginToLoginSettings(userName),
//                        )
//                    }catch (e: Exception){
//                        Timber.d("Failed to navigate"+e.message)
//                    }
//
//                }
//            } else {
//                binding.loginSettings.visibility = View.INVISIBLE
//            }
//        }



    }

}