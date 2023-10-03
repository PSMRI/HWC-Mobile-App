package org.piramalswasthya.cho.ui.home_activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.databinding.ActivityDashboardBinding
import org.piramalswasthya.cho.databinding.FragmentDashboardBinding
import org.piramalswasthya.cho.databinding.FragmentHomeBinding
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.PatientDetails
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    @Inject
    lateinit var benFlowDao: BenFlowDao
    private var maleOpdCount : Int? = 0
    private var femaleOpdCount : Int? = 0
    private var totalOpdCount : Int? = 0
    private val binding: FragmentDashboardBinding
        get() = _binding!!
    val months = listOf(
        "Today",
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )
    private var selectedPeriod : String = "Today"
    private var periodParam : String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

//         months = resources.getStringArray(R.array.months)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.drop_down,months)
        // get reference to the autocomplete text view
        // set adapter to the autocomplete tv to the arrayAdapter
        _binding!!.selectPeriod.setAdapter(arrayAdapter)
//        _binding!!.selectPeriod.setText(arrayAdapter.getItem(0), false);
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val autocompleteTV = binding.selectPeriod

        autocompleteTV.setOnItemClickListener { _, _, position, _ ->
            autocompleteTV.setText(months[position], false)
            selectedPeriod = autocompleteTV.text.toString()
            periodParam = if (position == 0) {
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            } else (if(position<10) {
                "-0$position-"
            }else{
                "-$position-"
            }).toString()

            lifecycleScope.launch {
                maleOpdCount = benFlowDao.getOpdCount(1, periodParam!!) ?: 0
                femaleOpdCount = benFlowDao.getOpdCount(2, periodParam!!) ?: 0
                totalOpdCount = maleOpdCount!! + femaleOpdCount!!

                binding.opdValue.text = totalOpdCount.toString()
                binding.opdMaleValue.text = maleOpdCount.toString()
                binding.opdFemaleValue.text = femaleOpdCount.toString()
            }
        }
//        binding.button.setOnClickListener {
//            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
//            currFragment.onSubmitAction()
//        }
//
//        binding.todayButton.setOnClickListener {
//            currFragment = navHostFragment.childFragmentManager.primaryNavigationFragment as NavigationAdapter
//            currFragment.onCancelAction()
//        }
    }

}