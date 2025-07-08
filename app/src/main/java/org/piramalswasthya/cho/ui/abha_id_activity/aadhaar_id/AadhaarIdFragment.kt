package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentAadhaarIdBinding
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel.State


@AndroidEntryPoint
class AadhaarIdFragment : Fragment() {

    private var _binding: FragmentAadhaarIdBinding? = null
    private val binding: FragmentAadhaarIdBinding
        get() = _binding!!

    private lateinit var abhaMode: AadhaarIdViewModel.Abha
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

        binding.createToggle.setOnClickListener {
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.tilAadhaarVerifyDropdown.visibility = View.VISIBLE
            binding.actvAadharVerificationDropdown.visibility = View.VISIBLE
            binding.searchToggle.setBackgroundResource(0)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.navHostFragmentFindAbha.visibility = View.GONE
            binding.navHostFragmentAadhaarId.visibility = View.VISIBLE

            viewModel.selectedNavToggle = "navHostFragmentAadhaarId"

        }
        binding.searchToggle.setOnClickListener {
            binding.createToggle.setBackgroundResource(0)
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.tilAadhaarVerifyDropdown.visibility = View.GONE
            binding.actvAadharVerificationDropdown.visibility = View.GONE

            binding.searchToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.navHostFragmentAadhaarId.visibility = View.GONE
            binding.navHostFragmentFindAbha.visibility = View.VISIBLE
            viewModel.selectedNavToggle = "navHostFragmentFindAbha"

        }

        binding.rgGovAsha.setOnCheckedChangeListener { _, id ->
            when (id) {
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

        binding.actvAadharVerificationDropdown.id = 0

        binding.actvAadharVerificationDropdown.setOnItemClickListener { _, _, i, _ ->
            when (i) {
                0 -> {
                    viewModel.setVerificationType("OTP")
                }

                1 -> {
                    viewModel.setVerificationType("FP")
                    binding.rgGovAsha.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.abhaMode.observe(viewLifecycleOwner) { mode->
            abhaMode = mode
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
                            if (abhaMode == AadhaarIdViewModel.Abha.CREATE) {
                                findNavController().navigate(
                                    AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarOtpFragment(
                                        viewModel.txnId, viewModel.mobileNumber
                                    )
                                )
                            } else {
                                findNavController().navigate(
                                    AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarOtpFragment(
                                        viewModel.txnId, viewModel.mobileNumber
                                    )
                                )
                            }
                        } else if (viewModel.verificationType.value == "FP") {
                            findNavController().navigate(
                                AadhaarIdFragmentDirections.actionAadhaarIdFragmentToGenerateMobileOtpFragment(
                                    viewModel.txnId, viewModel.mobileNumber
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
                            viewModel.txnId, "", "", "",""
                        )
                    )
                }
            }
        }

        viewModel.navigateToAadhaarConsent.observe(viewLifecycleOwner){
            if (it==true){
                findNavController().navigate(
                    AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarConsentFragment()
                )
                viewModel.navigateToAadhaarConsent(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as AbhaIdActivity).updateActionBar(
                R.drawable.ic__abha_logo_v1_24,
                getString(R.string.generate_abha)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()


        if (viewModel.selectedNavToggle == "navHostFragmentAadhaarId"){
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.tilAadhaarVerifyDropdown.visibility = View.VISIBLE
            binding.actvAadharVerificationDropdown.visibility = View.VISIBLE
            binding.searchToggle.setBackgroundResource(0)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.navHostFragmentFindAbha.visibility = View.GONE
            binding.navHostFragmentAadhaarId.visibility = View.VISIBLE

            viewModel.selectedNavToggle = "navHostFragmentAadhaarId"
        }else{
            binding.createToggle.setBackgroundResource(0)
            binding.searchToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondary))
            binding.createToggle.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_dark_shadow))
            binding.tilAadhaarVerifyDropdown.visibility = View.GONE
            binding.actvAadharVerificationDropdown.visibility = View.GONE

            binding.searchToggle.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.createToggle.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.searchToggle.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.navHostFragmentAadhaarId.visibility = View.GONE
            binding.navHostFragmentFindAbha.visibility = View.VISIBLE
            viewModel.selectedNavToggle = "navHostFragmentFindAbha"



        }
    }

}

