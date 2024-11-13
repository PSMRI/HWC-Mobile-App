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
    private var maleOpdCount: Int? = 0
    private var femaleOpdCount: Int? = 0
    private var othersOpdCount: Int? = 0
    private var totalOpdCount: Int? = 0

    private var ancCount: Int? = 0
    private var pncCount: Int? = 0
    private var immunizationCount: Int? = 0
    private var ectCount: Int? = 0

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
    private var selectedPeriod: String = "Today"
    private var periodParam: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.drop_down, months)

        _binding!!.selectPeriod.setAdapter(arrayAdapter)
        _binding!!.selectPeriod.setText(arrayAdapter.getItem(0), false);
        periodParam = SimpleDateFormat("yyyy-MM-dd").format(Date())
        lifecycleScope.launch {
            fetchAndDisplayCount()
        }
        return binding.root
    }

    private suspend fun fetchAndDisplayCount() {
        val user = userRepo.getLoggedInUser()
        if (user?.userName == null) {
            return
        }

        maleOpdCount = benFlowDao.getOpdCount(1, periodParam!!, user.userName) ?: 0
        femaleOpdCount = benFlowDao.getOpdCount(2, periodParam!!, user.userName) ?: 0
        othersOpdCount = benFlowDao.getOpdCount(3, periodParam!!, user.userName) ?: 0
        totalOpdCount = maleOpdCount!! + femaleOpdCount!! + othersOpdCount!!

        if (maleOpdCount!! > 0) binding.opdMaleValue.text =
            maleOpdCount.toString()
        if (maleOpdCount!! > 0) binding.opdFemaleValue.text =
            femaleOpdCount.toString()
        if (othersOpdCount!! > 0) binding.opdOtherValue.text =
            othersOpdCount.toString()
        if (totalOpdCount!! > 0) binding.opdTotalValue.text =
            totalOpdCount.toString()


        //Custom Design for Opd
        if (maleOpdCount!! > 0) binding.tvMaleCount.text =
            maleOpdCount.toString()
        if (maleOpdCount!! > 0) binding.tvFemaleCount.text =
            femaleOpdCount.toString()
        if (othersOpdCount!! > 0) binding.tvOtherCount.text =
            othersOpdCount.toString()
        if (totalOpdCount!! > 0) binding.tvOpdCount.text =
            totalOpdCount.toString()

        ancCount = benFlowDao.getAncCount(periodParam!!, user.userName) ?: 0
        pncCount = benFlowDao.getPncCount(periodParam!!, user.userName) ?: 0
        immunizationCount = benFlowDao.getImmunizationCount(periodParam!!, user.userName) ?: 0
        ectCount = benFlowDao.getEctCount(periodParam!!, user.userName) ?: 0

        if (ancCount!! > 0) binding.tvAncValue.text = ancCount.toString()
        if (pncCount!! > 0) binding.tvPncValue.text = pncCount.toString()
        if (immunizationCount!! > 0) binding.tvImmValue.text = immunizationCount.toString()
        if (ectCount!! > 0) binding.tvFamValue.text = ectCount.toString()

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