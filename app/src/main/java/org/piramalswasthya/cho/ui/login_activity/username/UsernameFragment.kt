package org.piramalswasthya.cho.ui.login_activity.username

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentUsernameBinding
import javax.inject.Inject

@AndroidEntryPoint
class UsernameFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    private lateinit var viewModel: UsernameViewModel

    private var _binding: FragmentUsernameBinding? = null
    private val binding: FragmentUsernameBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(UsernameViewModel::class.java)
        _binding = FragmentUsernameBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cbRememberUsername:Boolean = binding.cbRemember.isChecked
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
//            if(cbRememberUsername.isChecked) {
//                viewModel.rememberUser(binding.etUsername.text.toString())
//                viewModel.fetchRememberedUserName()?.let {
//                    binding.etUsername.setText(it)
//                }
//            }
            if(!binding.etUsername.text.toString().isNullOrBlank())
            findNavController().navigate(
                UsernameFragmentDirections.actionSignInFragmentToChoLogin(binding.etUsername.text.toString())
            )
            else
                Toast.makeText(requireContext(), "Invalid Username!!", Toast.LENGTH_LONG).show()
        }


    }


}

//private fun NavController.navigate(putString: Unit) {
//
//}
