package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.aadhaar_consent

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.AadhaarConsentAdapter
import org.piramalswasthya.cho.databinding.FragmentAbhaConsentBinding
import org.piramalswasthya.cho.model.AadhaarConsentModel
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel


class AbhaConsentFragment : Fragment() {

    private var _binding: FragmentAbhaConsentBinding? = null
    private val binding: FragmentAbhaConsentBinding
        get() = _binding!!

    val viewModel: AbhaConsentViewModel by viewModels({ requireActivity() })
    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })
    private var count: Int = 0
    private lateinit var navController: NavController

    lateinit var recyclerList:List<AadhaarConsentModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbhaConsentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)
        setUpAadhaarConsentRvAdapter()
        binding.btnAccept.isEnabled = true
        binding.btnAccept.setOnClickListener {
            parentViewModel.setConsentChecked(true)
            navController.navigateUp()
        }
    }

    private fun setUpAadhaarConsentRvAdapter() {
        recyclerList = getConsentList()
        val rvLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = rvLayoutManager
        val rvAdapter =
            AadhaarConsentAdapter(AadhaarConsentAdapter.ConsentClickListener { item, position ->

                if (position == 0){
                    if (item.checked){
                        recyclerList.forEach {
                            it.checked = true
                        }
                        binding.recyclerView.adapter?.notifyDataSetChanged()
                        binding.btnAccept.isEnabled = true
                    }else{
                        recyclerList.forEach {
                            it.checked = false
                        }
                        binding.recyclerView.adapter?.notifyDataSetChanged()
                        binding.btnAccept.isEnabled = false

                    }
                }else{
                    if (item.checked) {
                        count += 1
                        if (count >= 7) {
                            binding.btnAccept.isEnabled = true
                        }
                    } else {
                        count -= 1
                        binding.btnAccept.isEnabled = false
                    }
                }

            })
        binding.recyclerView.adapter = rvAdapter
        (binding.recyclerView.adapter as AadhaarConsentAdapter?)?.submitList(recyclerList)
    }


    fun getConsentList(): List<AadhaarConsentModel> {
        val consent6 = resources.getString(R.string.str_aadhaar_consent_6)
        val userName = viewModel.currentUser?.name ?: ""
        val updatedText = consent6.replace("@ashaName", userName)
        var usernameBoldItalic = formatUserNameBoldItalic(updatedText, userName)

        var consent7 = resources.getString(R.string.str_aadhaar_consent_7)
        val beneficiaryName = parentViewModel.beneficiaryName.value ?: ""
        val updatedText1 = consent7.replace("@beneficiaryName", beneficiaryName)
        var beneficiaryNameBoldItalic = formatUserNameBoldItalic(updatedText1, beneficiaryName)

        return listOf(
            AadhaarConsentModel(resources.getString(R.string.str_aadhaar_consent_1)),
            AadhaarConsentModel(resources.getString(R.string.str_aadhaar_consent_2)),
            AadhaarConsentModel(resources.getString(R.string.str_aadhaar_consent_3)),
            AadhaarConsentModel(resources.getString(R.string.str_aadhaar_consent_4)),
            AadhaarConsentModel(resources.getString(R.string.str_aadhaar_consent_5)),
            AadhaarConsentModel(usernameBoldItalic),
            AadhaarConsentModel(beneficiaryNameBoldItalic)
        )
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as AbhaIdActivity).updateActionBar(
                R.drawable.ic__abha_logo_v1_24,
                "Consent"
            )
        }
    }

    fun formatUserNameBoldItalic(fullText: String, userName: String): SpannableString {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(userName)
        val endIndex = startIndex + userName.length
        if (startIndex != -1) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD_ITALIC),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_theme_light_primary
                    )
                ),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}