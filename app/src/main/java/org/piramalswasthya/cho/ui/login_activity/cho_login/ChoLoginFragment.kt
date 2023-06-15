package org.piramalswasthya.cho.ui.login_activity.cho_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.ui.login_activity.cho_login.hwc.HwcFragment
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachFragment
import javax.inject.Inject


@AndroidEntryPoint
class ChoLoginFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentChoLoginBinding? = null
    private val binding: FragmentChoLoginBinding
        get() = _binding!!

//    private lateinit val fragmentManager = requireActivity().supportFragmentManager
//    private val fragmentTransaction = fragmentManager.beginTransaction()

//    private lateinit var fragmentManager : FragmentManager
//    private lateinit var fragmentTransaction : FragmentTransaction

    private val hwcFragment = HwcFragment()
    private val outreachFragment = OutreachFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChoLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

//    fun setActivityContainer(i: Programs){
//        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
//        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
//        when (i){
//            Programs.HWC_RADIO -> {
//                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
//                fragmentTransaction.commit()
//            }
//            Programs.OUTREACH_RADIO -> {
//                fragmentTransaction.replace(binding.selectActivityContainer.id, outreachFragment)
//                fragmentTransaction.commit()
//            }
//            else -> {
//                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
//                fragmentTransaction.commit()
//            }
//        }
//    }


    fun setActivityContainer(programId: Int){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


//        setActivityContainer(Programs.HWC_RADIO)
//        binding.selectProgram.setOnCheckedChangeListener { _, i ->
//            val selectedProgram = when (i) {
//                binding.btnHwc.id -> Programs.HWC_RADIO
//                binding.btnOutreach.id -> Programs.OUTREACH_RADIO
//                else -> Programs.HWC_RADIO
//            }
//            setActivityContainer(selectedProgram)
//        }

        setActivityContainer(binding.selectProgram.id)
        binding.selectProgram.setOnCheckedChangeListener { _, programId ->
//            val selectedProgram = when (i) {
//                binding.btnHwc.id -> Programs.HWC_RADIO
//                binding.btnOutreach.id -> Programs.OUTREACH_RADIO
//                else -> Programs.HWC_RADIO
//            }
            setActivityContainer(programId)
        }

//        setActivityContainer(binding.selectProgram.id)
//        binding.selectProgram.setOnCheckedChangeListener { _, i ->
//            setActivityContainer(i)
//
//            when (i) {
//                binding.btnHwc.id -> {
//                    print("button id is " + binding.selectProgram.id)
//                    findNavController().navigate(
//                        UsernameFragmentDirections.actionSignInFragmentToChoLogin()
//                    )
//                }
//                binding.btnOutreach.id -> {
//                    print("button id is " + binding.selectProgram.id)
//                    findNavController().navigate(
//                        UsernameFragmentDirections.actionSignInFragmentToChoLogin()
//                    )
//                }
//                else -> {
//                    binding.selectProgram.id = binding.btnHwc.id
//
//                }
//            }
//
//        }

    }

}