package org.piramalswasthya.cho.ui.login_activity.cho_login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.repositories.UserAuthRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.login_activity.cho_login.hwc.HwcFragment
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachFragment
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ChoLoginFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

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
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        val hwcFragment  = HwcFragment(userName)
        val outreachFragment = OutreachFragment(userName);
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
    }

}