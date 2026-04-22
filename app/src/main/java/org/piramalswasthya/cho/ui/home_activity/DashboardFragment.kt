package org.piramalswasthya.cho.ui.home_activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.databinding.FragmentDashboardBinding
import org.piramalswasthya.cho.repositories.UserRepo
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    @Inject
    lateinit var benFlowDao: BenFlowDao
    @Inject
    lateinit var userRepo: UserRepo
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

    private val months: List<String>
        get() = listOf(
            getString(R.string.today),
            getString(R.string.month_january),
            getString(R.string.month_february),
            getString(R.string.month_march),
            getString(R.string.month_april),
            getString(R.string.month_may),
            getString(R.string.month_june),
            getString(R.string.month_july),
            getString(R.string.month_august),
            getString(R.string.month_september),
            getString(R.string.month_october),
            getString(R.string.month_november),
            getString(R.string.month_december)
        )
    private var selectedPeriod: String = ""
    private var periodParam : String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        selectedPeriod = getString(R.string.today)
        val monthsList = months
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.drop_down, monthsList)

        _binding!!.selectPeriod.setAdapter(arrayAdapter)
        _binding!!.selectPeriod.setText(arrayAdapter.getItem(0), false)
        periodParam = SimpleDateFormat("yyyy-MM-dd").format(Date())
        lifecycleScope.launch {
            fetchAndDisplayCount()
        }
        return binding.root
    }
    private suspend fun fetchAndDisplayCount(){
        val user = userRepo.getLoggedInUser()
        if(user?.userName == null){
            return
        }
        maleOpdCount = benFlowDao.getDoctorModuleOpdCount("male") ?: 0
        femaleOpdCount = benFlowDao.getDoctorModuleOpdCount("female") ?: 0
        othersOpdCount = benFlowDao.getDoctorModuleOpdCount("other") ?: 0
        totalOpdCount = maleOpdCount!! + femaleOpdCount!! + othersOpdCount!!

        binding.opdMaleValue.text = maleOpdCount.toString()
        binding.opdFemaleValue.text = femaleOpdCount.toString()
        binding.opdOtherValue.text = othersOpdCount.toString()
        binding.opdTotalValue.text = totalOpdCount.toString()

        ancCount = benFlowDao.getAncCount(periodParam!!, user.userName) ?: 0
        pncCount = benFlowDao.getPncCount(periodParam!!, user.userName) ?: 0
        immunizationCount = benFlowDao.getImmunizationCount(periodParam!!, user.userName) ?: 0
        ectCount = benFlowDao.getEctCount(periodParam!!, user.userName) ?: 0

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                benFlowDao.observeDoctorModuleListCount().collect {
                    fetchAndDisplayCount()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            fetchAndDisplayCount()
        }
    }
}