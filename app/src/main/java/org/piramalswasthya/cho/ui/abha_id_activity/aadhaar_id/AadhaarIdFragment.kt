package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentAadhaarIdBinding
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel.State


@AndroidEntryPoint
class AadhaarIdFragment : Fragment() {

    private var _binding: FragmentAadhaarIdBinding? = null
    private val binding: FragmentAadhaarIdBinding
        get() = _binding!!

    private val viewModel: AadhaarIdViewModel by viewModels({requireActivity()})

    private lateinit var navController: NavController
    private val aadhaarNavController by lazy {
        val navHostFragment: NavHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_aadhaar_id) as NavHostFragment
        navHostFragment.navController
    }
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAadhaarIdBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.viewModel = viewModel

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        binding.rgGovAsha.setOnCheckedChangeListener{
                _, id ->
            when(id) {
                R.id.rb_asha -> {
                    viewModel.setUserType("ASHA")
                    aadhaarNavController.navigate(R.id.aadhaarNumberAshaFragment)
                }
                R.id.rb_gov -> {
                    viewModel.setUserType("GOV")
                    aadhaarNavController.navigate(R.id.aadhaarNumberGovFragment)
                }
            }
        }

        binding.actvAadharVerificationDropdown.setOnItemClickListener { _, _, i, _ ->
            when(i) {
                0 -> {
                    viewModel.setVerificationType("OTP")
                    Toast.makeText(requireContext(), viewModel.verificationType.value, Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    viewModel.setVerificationType("FP")
                    binding.rgGovAsha.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {}
                State.LOADING -> {
                    binding.clContentAadharId.visibility = View.INVISIBLE
                    binding.pbLoadingAadharId.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.SUCCESS -> {
                    if (viewModel.userType.value == "ASHA") {
                        viewModel.resetState()
                        if (viewModel.verificationType.value == "OTP") {
                            findNavController().navigate(
                                AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarOtpFragment(
                                    viewModel.txnId, viewModel.mobileNumber
                                )
                            )
                        } else if (viewModel.verificationType.value == "FP") {
                            findNavController().navigate(
                                AadhaarIdFragmentDirections.actionAadhaarIdFragmentToGenerateMobileOtpFragment(
                                    viewModel.txnId
                                )
                            )
                        }
                    }
                }
                State.ERROR_SERVER -> {
                    binding.pbLoadingAadharId.visibility = View.INVISIBLE
                    binding.clContentAadharId.visibility = View.VISIBLE
                    binding.clError.visibility = View.INVISIBLE
                }
                State.ERROR_NETWORK -> {
                    binding.clContentAadharId.visibility = View.INVISIBLE
                    binding.pbLoadingAadharId.visibility = View.INVISIBLE
                    binding.clError.visibility = View.VISIBLE
                }
                State.STATE_DETAILS_SUCCESS -> {
                    binding.clContentAadharId.visibility = View.VISIBLE
                    binding.pbLoadingAadharId.visibility = View.INVISIBLE
                }
                State.ABHA_GENERATED_SUCCESS -> {
                    findNavController().navigate(
                        AadhaarIdFragmentDirections.actionAadhaarIdFragmentToCreateAbhaFragment(
                            viewModel.txnId,
                            viewModel.userType.value!!,
                            viewModel.abha.value!!.name,
                            viewModel.abha.value!!.healthIdNumber,
                        )
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}

