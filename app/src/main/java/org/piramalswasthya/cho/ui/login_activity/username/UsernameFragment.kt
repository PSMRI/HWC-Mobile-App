package org.piramalswasthya.cho.ui.login_activity.username

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentUsernameBinding
import org.piramalswasthya.cho.helpers.Languages
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.login_activity.LoginActivity
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private var locationManager: LocationManager? = null
private var locationListener: LocationListener? = null
private var currentLocation: Location? = null
private var tryAuthUser = false

@AndroidEntryPoint
class UsernameFragment() : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    @Inject
    lateinit var userDao: UserDao
    @Inject
    lateinit var userRepo: UserRepo
    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository
    private var loginSettingsData: LoginSettingsData? = null

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var viewModel: UsernameViewModel
    private var user: UserCache? = null
    private var prevLoggedInUser: UserCache? = null
    private var showDashboard : Boolean? = null
    private var isBiometric : Boolean = false
    private var _binding: FragmentUsernameBinding? = null
    private val binding: FragmentUsernameBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getCurrentLocation()

        lifecycleScope.launch {
                    if(userRepo.getLoggedInUser() == null){
                    user = userDao.getLastLoggedOutUser()
                    if(user!=null) {
                        binding.etUsername.setText(user?.userName)
                        biometricPrompt.authenticate(promptInfo)
                    }
                }
                }
        viewModel = ViewModelProvider(this).get(UsernameViewModel::class.java)
        _binding = FragmentUsernameBinding.inflate(layoutInflater, container, false)
//        binding.loginSettings.visibility = View.INVISIBLE

        if(!viewModel.fetchRememberedUserName().isNullOrBlank()) {
            viewModel.fetchRememberedUserName()?.let {
                binding.etUsername.setText(it)
                binding.cbRemember.isChecked = true
            }
        }
        if(binding.etUsername.text.isNullOrBlank()) {
            binding.btnNxt.isEnabled = false
            binding.cbRemember.isChecked = false
//            binding.loginSettings.isEnabled = false
        }
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->{}
//                displayMessage("Biometric authentication is available")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                displayMessage("This device doesn't support biometric authentication")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                displayMessage("Biometric authentication is currently unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                displayMessage("No biometric credentials are enrolled")
        }
        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                displayMessage("Authentication error: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isBiometric = true
                val username = user?.userName
                val password = user?.password

                getCurrentLocation()
                tryAuthUser = true
                viewModel.state.observe(viewLifecycleOwner) { state ->
                    when (state!!) {
                        OutreachViewModel.State.SUCCESS -> {
                            var cbRememberUsername:Boolean = binding.cbRemember.isChecked
                            findNavController().navigate(
                                UsernameFragmentDirections.actionSignInFragmentToChoLogin(
                                    binding.etUsername.text.toString(),
                                    cbRememberUsername,
                                    isBiometric
                                )
                            )
                            isBiometric = false
                        }
                        else -> {}
                    }
                }
                displayMessage("Authentication succeeded!")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                displayMessage("Authentication failed")
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        return binding.root
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    }
    private fun displayMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getOutreach()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        binding.etUsername .addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.btnNxt.isEnabled = !s.isNullOrBlank()
//                binding.loginSettings.isEnabled = !s.isNullOrBlank()
//                val userName = (s.toString())!!;
//                if(!s.isNullOrBlank()){
//                    lifecycleScope.launch {
//                        loginSettingsData =  loginSettingsDataRepository.getLoginSettingsDataByUsername(userName)
//
//                        if (loginSettingsData==null) {
//
//                            binding.loginSettings.visibility = View.VISIBLE
//
//                            binding.loginSettings.setOnClickListener{
//                                try {
//                                    findNavController().navigate(
//                                        UsernameFragmentDirections.actionUsernameFragmentToLoginSettings(binding.etUsername.text.toString()),
//                                    )
//                                }catch (e: Exception){
//                                    Timber.d("Failed to navigate"+e.message)
//                                }
//
//                            }
//                        } else {
//                            binding.loginSettings.visibility = View.INVISIBLE
//                            binding.btnNxt.isEnabled = true
//                        }
//                    }
//                }
//                else{
//                    binding.loginSettings.visibility = View.INVISIBLE
//                    binding.btnNxt.isEnabled = false
//                }
        }
        })
        binding.btnNxt.setOnClickListener {
            if(!binding.etUsername.text.toString().isNullOrBlank()) {
                var cbRememberUsername:Boolean = binding.cbRemember.isChecked
                findNavController().navigate(
                    UsernameFragmentDirections.actionSignInFragmentToChoLogin(
                        binding.etUsername.text.toString(),
                        cbRememberUsername,
                        isBiometric
                    )
                )
            }
            else
                Toast.makeText(requireContext(), getString(R.string.invalid_username_entered), Toast.LENGTH_LONG).show()
        }
//
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
        when (prefDao.getCurrentLanguage()) {
            Languages.ENGLISH -> binding.rgLangSelect.check(binding.rbEng.id)
            Languages.KANNADA -> binding.rgLangSelect.check(binding.rbKannada.id)
        }

        binding.rgLangSelect.setOnCheckedChangeListener { _, i ->
            val currentLanguage = when (i) {
                binding.rbEng.id -> Languages.ENGLISH
                binding.rbKannada.id -> Languages.KANNADA
                else -> Languages.ENGLISH
            }
            prefDao.saveSetLanguage(currentLanguage)
            Locale.setDefault(Locale(currentLanguage.symbol))

            val refresh = Intent(requireContext(), LoginActivity::class.java)
            Log.d("refresh Called!",Locale.getDefault().language)
            requireActivity().finish()
            startActivity(refresh)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)


        }
    }

private fun getCurrentLocation() {
    // Check if location permissions are granted
    if (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
        PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
        PackageManager.PERMISSION_GRANTED
    ) {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //  Location listener
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
                if (tryAuthUser) {
                    viewModel.authUser(
                        user?.userName ?: "",
                        user?.password ?: "",
                        null,
                        null,
                        DateTimeUtil.formatDate(Date()),
                        null,
                        location.latitude,
                        location.longitude,
                        null,
                        requireContext()
                    )
                    tryAuthUser = false
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
                Toast.makeText(
                    requireContext(), "Location Provider/GPS disabled",
                    Toast.LENGTH_SHORT
                ).show()
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
            }
        }

        // Request location updates
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            locationListener!!
        )
    } else {
        // Request location permissions if not granted
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }
}
}


