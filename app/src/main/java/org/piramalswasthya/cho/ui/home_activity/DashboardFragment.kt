package org.piramalswasthya.cho.ui.home_activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    @Inject
    lateinit var benFlowDao: BenFlowDao
    private var maleOpdCount : Int? = 0
    private var femaleOpdCount : Int? = 0
    private var othersOpdCount : Int? = 0
    private var totalOpdCount : Int? = 0

    private var ancCount : Int? = 0
    private var pncCount : Int? = 0
    private var immunizationCount : Int? = 0
    private var ectCount : Int? = 0

    private val binding: FragmentDashboardBinding
        get() = _binding!!
    lateinit var viewModel: DashboardViewModel

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
        _binding!!.selectPeriod.setText(arrayAdapter.getItem(0), false);
        periodParam = SimpleDateFormat("yyyy-MM-dd").format(Date())
        lifecycleScope.launch {
            fetchAndDisplayCount()
        }
        return binding.root
    }
    private suspend fun fetchAndDisplayCount(){
        maleOpdCount = benFlowDao.getOpdCount(1, periodParam!!) ?: 0
        femaleOpdCount = benFlowDao.getOpdCount(2, periodParam!!) ?: 0
        othersOpdCount = benFlowDao.getOpdCount(3, periodParam!!) ?: 0
        totalOpdCount = maleOpdCount!! + femaleOpdCount!! + othersOpdCount!!

        binding.opdMaleValue.text = maleOpdCount.toString()
        binding.opdFemaleValue.text = femaleOpdCount.toString()
        binding.opdOtherValue.text = othersOpdCount.toString()
        binding.opdTotalValue.text = totalOpdCount.toString()

        ancCount = benFlowDao.getAncCount(periodParam!!) ?: 0
        pncCount = benFlowDao.getPncCount(periodParam!!) ?: 0
        immunizationCount = benFlowDao.getImmunizationCount(periodParam!!) ?: 0
        ectCount = benFlowDao.getEctCount(periodParam!!) ?: 0

        binding.tvAncValue.text = ancCount.toString()
        binding.tvPncValue.text = pncCount.toString()
        binding.tvImmValue.text = immunizationCount.toString()
        binding.tvFamValue.text = ectCount.toString()


    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val autocompleteTV = binding.selectPeriod
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)


        autocompleteTV.setOnItemClickListener { _, _, position, _ ->
            autocompleteTV.setText(months[position], false)
            selectedPeriod = autocompleteTV.text.toString()
            periodParam = if (position == 0) {
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            } else (if (position < 10) {
                "-0$position-"
            } else {
                "-$position-"
            }).toString()

            lifecycleScope.launch {
                fetchAndDisplayCount()
            }

        }
    }
}