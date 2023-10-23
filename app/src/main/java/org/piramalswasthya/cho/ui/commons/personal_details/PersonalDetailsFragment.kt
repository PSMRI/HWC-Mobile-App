package org.piramalswasthya.cho.ui.commons.personal_details

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.network.interceptors.TokenESanjeevaniInterceptor
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.home.HomeViewModel
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject


@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {
    @Inject
    lateinit var apiService : ESanjeevaniApiService
    private lateinit var viewModel: PersonalDetailsViewModel
    private lateinit var homeviewModel: HomeViewModel
    private var itemAdapter : PatientItemAdapter? = null
    private var usernameEs : String = ""
    private var passwordEs : String = ""
    private var errorEs : String = ""
    private var network : Boolean = false

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private var _binding: FragmentPersonalDetailsBinding? = null
    private var patientCount : Int = 0

    private val binding
        get() = _binding!!

    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }
//    private val parentViewModel: HomeViewModel by lazy {
//        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
//    }
//private val parentViewModel: HomeViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        HomeViewModel.resetSearchBool()
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        homeviewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
//        parentViewModel.searchBool.observe((viewLifecycleOwner)){
        HomeViewModel.searchBool.observe(viewLifecycleOwner){
            bool ->
            when(bool!!) {
                true ->{
//                    binding.search.post {
//                        lifecycleScope.launch {
//                            withContext(Dispatchers.IO){
//                                delay(5000)
//                            }
                            binding.search.requestFocus()
                            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                        }

//                    }
//                }
//                    Handler(Looper.getMainLooper()).postDelayed(
//                    {
//                    binding.search.requestFocus()
//            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//            imm?.showSoftInput(binding.search, InputMethodManager.SHOW_FORCED);
//                }
//            , 100)
                else -> {}
            }

        }
        binding.searchTil.setEndIconOnClickListener {
            speechToTextLauncherForSearchByName.launch(Unit)
        }
        viewModel = ViewModelProvider(this).get(PersonalDetailsViewModel::class.java)
        viewModel.patientObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PersonalDetailsViewModel.NetworkState.SUCCESS -> {
                    var result = ""
                    if(itemAdapter?.itemCount==0||itemAdapter?.itemCount==1) {
                        result = getString(R.string.patient_cnt_display)
                    }
                    else {
                        result = getString(R.string.patients_cnt_display)
                    }
                     itemAdapter = context?.let { it ->
                         PatientItemAdapter(
                            apiService,
                            it,
//                            onItemClicked = {
//                                val intent = Intent(context, EditPatientDetailsActivity::class.java)
//                                intent.putExtra("patientId", it.patient.patientID);
//                                startActivity(intent)
//                            },
                            clickListener = PatientItemAdapter.BenClickListener(
                            {
                                benVisitInfo ->
                                    if(benVisitInfo.nurseFlag == null){
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", benVisitInfo);
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                    else if((benVisitInfo.pharmacist_flag == 1 && benVisitInfo.doctorFlag == 9 && preferenceDao.isPharmacist()) ||
                                        (benVisitInfo.pharmacist_flag == 1 && benVisitInfo.doctorFlag == 9 && preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Pharmacist")){
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", benVisitInfo);
                                        startActivity(intent)
                                    }
                                    else if((benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 1) ||
                                        (benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 1 && preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor")){
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", benVisitInfo);
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                    else if((benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 2 && preferenceDao.isStartingLabTechnician()) ||
                                        (benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 2 && preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Lab Technician")){
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", benVisitInfo);
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                    else if(benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 2){
                                         Toast.makeText(
                                            requireContext(),
                                            resources.getString(R.string.pendingForLabtech),
                                            Toast.LENGTH_SHORT
                                         ).show()
                                    }
                                    else if((benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 3) ||
                                        (benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 3 && preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor")){
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", benVisitInfo);
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                    else if(benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 9){
                                        Toast.makeText(
                                            requireContext(),
                                            resources.getString(R.string.flowCompleted),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                else{
//                                        Timber.d("*******************Babs DTO************** ", Gson().toJson(benVisitInfo))
                                    }

                            },
                            {
                                benVisitInfo ->
                                    Log.d("ben click listener", "ben click listener")
                                    checkAndGenerateABHA(benVisitInfo)
                            },
                            {
                                    benVisitInfo -> callLoginDialog(benVisitInfo)
                            }
                         ),
                            showAbha = true
                        )
                    }
                    binding.patientListContainer.patientList.adapter = itemAdapter
                    if(preferenceDao.isUserOnlyDoctorOrMo() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Doctor")) {
                        lifecycleScope.launch {
                            viewModel.patientListForDoctor?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    it.size.toString() + getResultStr(it.size)
                                patientCount = it.size
                            }
                        }
                    }
                    else if (preferenceDao.isStartingLabTechnician() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Lab Technician")) {
                        lifecycleScope.launch {
                            viewModel.patientListForLab?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    it.size.toString() + getResultStr(it.size)
                                patientCount = it.size
                            }
                        }
                    }
                    else if (preferenceDao.isPharmacist() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Pharmacist")) {
                        lifecycleScope.launch {
                            viewModel.patientListForPharmacist?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
                                patientCount = it.size
                            }
                        }
                    }
                    else if (preferenceDao.isUserOnlyNurseOrCHO() || (preferenceDao.isCHO() && preferenceDao.getCHOSecondRole() == "Nurse")){
                        lifecycleScope.launch {
                            viewModel.patientListForNurse?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    it.size.toString() + getResultStr(it.size)
                                patientCount = it.size
                            }
                        }
                    }
                    else {
                        lifecycleScope.launch {
                            viewModel.patientListForNurse?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    it.size.toString() + getResultStr(it.size)
                                patientCount = it.size
                            }
                        }
                    }

                }

                else -> {

                }
            }

            viewModel.abha.observe(viewLifecycleOwner) {
                it.let {
                    if (it != null) {
                        abhaDisclaimer.setMessage(it)
                        abhaDisclaimer.show()
                    }
                }
            }

            viewModel.benRegId.observe(viewLifecycleOwner) {
                if (it != null) {
                    val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("benId", viewModel.benId.value)
                    intent.putExtra("benRegId", it)
                    requireActivity().startActivity(intent)
                    viewModel.resetBenRegId()
                }
            }

            binding.search.setOnFocusChangeListener { searchView, b ->
                if (b)
                    (searchView as EditText).addTextChangedListener(searchTextWatcher)
                else
                    (searchView as EditText).removeTextChangedListener(searchTextWatcher)

            }
        }
    }
    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            viewModel.filterText(p0?.toString() ?: "")
            binding.patientListContainer.patientCount.text =
                patientCount.toString() + getResultStr(patientCount)
            Log.d("arr","${patientCount}")
        }

    }
    fun getResultStr(count:Int?):String{
        if(count==1||count==0){
            return getString(R.string.patient_cnt_display)
        }
        return getString(R.string.patients_cnt_display)
    }
    private val speechToTextLauncherForSearchByName = registerForActivityResult(SpeechToTextContract()) { result ->
        if (result.isNotBlank() && result.isNotEmpty() && !result.any { it.isDigit() }) {
            binding.search.setText(result)
            binding.search.addTextChangedListener(searchTextWatcher)
        }
    }
    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    private fun callLoginDialog(benVisitInfo: PatientDisplayWithVisitInfo) {
        if (benVisitInfo.patient.phoneNo.isNullOrEmpty()) {
            context?.let {
                MaterialAlertDialogBuilder(it).setTitle(getString(R.string.alert_popup))
                    .setMessage(getString(R.string.phone_no_not_found))
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                    }.create()
                    .show()
            }
        } else{
        network = isInternetAvailable(requireContext())
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_esanjeevani_login, null)
        val dialog = context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("eSanjeevani Login")
                .setView(dialogView)
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle cancel button click
                    dialog.dismiss()
                }
                .create()
        }
        dialog?.show()
            val loginBtn = dialogView.findViewById<MaterialButton>(R.id.loginButton)
            val rememberMeEsanjeevani = dialogView.findViewById<CheckBox>(R.id.cb_remember_es)
        if (network) {
            // Internet is available
            dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility = View.GONE
            dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.VISIBLE
            val rememberedUsername : String? = viewModel.fetchRememberedUsername()
            val rememberedPassword : String? = viewModel.fetchRememberedPassword()
            if(!rememberedUsername.isNullOrBlank() && !rememberedPassword.isNullOrBlank()){
                dialogView.findViewById<TextInputEditText>(R.id.et_username_es).text = Editable.Factory.getInstance().newEditable(rememberedUsername)
                dialogView.findViewById<TextInputEditText>(R.id.et_password_es).text = Editable.Factory.getInstance().newEditable(rememberedPassword)
                rememberMeEsanjeevani.isChecked = true
            }
        } else {
            dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.GONE
            dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility = View.VISIBLE
        }


        loginBtn.setOnClickListener {

            usernameEs =
                dialogView.findViewById<TextInputEditText>(R.id.et_username_es).text.toString()
                    .trim()
            passwordEs =
                dialogView.findViewById<TextInputEditText>(R.id.et_password_es).text.toString()
                    .trim()
            if(rememberMeEsanjeevani.isChecked){
                viewModel.rememberUserEsanjeevani(usernameEs,passwordEs)
            }else{
                viewModel.forgetUserEsanjeevani()
            }
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    var passWord = encryptSHA512(encryptSHA512(passwordEs) + encryptSHA512("token"))

                    var networkBody = NetworkBody(
                        usernameEs,
                        passWord,
                        "token",
                        "11001"
                    )
                    val errorTv = dialogView.findViewById<MaterialTextView>(R.id.tv_error_es)
                    network = isInternetAvailable(requireContext())
                    if (!network) {
                        errorTv.text = requireContext().getString(R.string.network_error)
                        errorTv.visibility = View.VISIBLE
                    } else {
                        errorTv.text = ""
                        errorTv.visibility = View.GONE
                        val responseToken = apiService.getJwtToken(networkBody)
                        if (responseToken.message == "Success") {
                            val token = responseToken.model?.access_token;
                            if (token != null) {
                                TokenESanjeevaniInterceptor.setToken(token)
                            }
                            val intent = Intent(context, WebViewActivity::class.java)
                            intent.putExtra("patientId", benVisitInfo.patient.patientID);
                            intent.putExtra("usernameEs", usernameEs);
                            intent.putExtra("passwordEs", passwordEs);
                            context?.startActivity(intent)
                            dialog?.dismiss()
                        } else {
                            errorEs = responseToken.message
                            errorTv.text = errorEs
                            errorTv.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Timber.d("GHere is error $e")
                }
            }
        }
    }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    private fun checkAndGenerateABHA(benVisitInfo: PatientDisplayWithVisitInfo) {
        Log.d("checkAndGenerateABHA click listener","checkAndGenerateABHA click listener")
        viewModel.fetchAbha(benVisitInfo.patient.beneficiaryID!!)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                // hide the soft keyboard when the navigation drawer is shown on the screen.
//                binding.search.clearFocus()
//                true
//            }
//
//            else -> false
//        }
//    }

}
