package org.piramalswasthya.cho.ui.commons.patient_home


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentPatientHomeBinding
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsViewModel
import org.piramalswasthya.cho.ui.home.HomeViewModel
import javax.inject.Inject


@AndroidEntryPoint
class PatientHomeFragment : Fragment() {

    private lateinit var viewModel: PatientHomeViewModel
    private lateinit var homeviewModel: HomeViewModel

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private var _binding: FragmentPatientHomeBinding? = null

    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        HomeViewModel.resetSearchBool()
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
