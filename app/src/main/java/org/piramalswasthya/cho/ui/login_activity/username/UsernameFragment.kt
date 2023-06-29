package org.piramalswasthya.cho.ui.login_activity.username

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentUsernameBinding
import org.piramalswasthya.cho.model.ModelObject
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class UsernameFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    @Inject
    lateinit var apiService: AmritApiService

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var _binding: FragmentUsernameBinding? = null
    private val binding: FragmentUsernameBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsernameBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.etUsername .addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.btnNxt.isEnabled = !s.isNullOrBlank()
            }
        })
        binding.btnNxt.setOnClickListener {
            findNavController().navigate(
                UsernameFragmentDirections.actionSignInFragmentToChoLogin(binding.etUsername.text.toString()),
            )
        }

        // button for navigate to web-view page of eSanjeevani
        binding.btnWebview.setOnClickListener {
            var user = "Cdac@1234";
            var token = "token"
            var passWord = encryptSHA512(encryptSHA512(user) + encryptSHA512(token))

            //creating object using encrypted Password and other details
            var networkBody = NetworkBody(
                "8501258162",
                passWord,
                "token",
                "11001"
            )
            var response: ModelObject

            // calling getAuthRefIdForWebView() in coroutine scope for getting referenceId
            coroutineScope.launch {
                try {
                    response = apiService.getAuthRefIdForWebView(networkBody)
                    if(response != null){
                        var referenceId = response.model.referenceId
                        var url = "https://uat.esanjeevani.in/#/external-provider-signin/$referenceId"
                        Timber.d("$url")
                        findNavController().navigate(
                            UsernameFragmentDirections.actionWebviewFragment(url)
                        )
                    }
                } catch (e : Exception){
                    Timber.d("Not able to fetch the data due to $e")
                }
            }
        }
    }

    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }


}

//private fun NavController.navigate(putString: Unit) {
//
//}
