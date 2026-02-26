package org.piramalswasthya.cho.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.piramalswasthya.cho.ui.home.HomeFragment
import org.piramalswasthya.cho.ui.home.RMNCHAFragment
import org.piramalswasthya.cho.ui.home_activity.DashboardFragment

class ViewPagerAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(supportFragmentManager, lifecycle) {

    // declare arrayList to contain fragments and its title
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun getItemCount(): Int {
        return  3
    }

    override fun createFragment(position: Int): Fragment {
       return when(position) {
           0 -> HomeFragment()
           1 -> DashboardFragment()
           2 -> RMNCHAFragment()
           else -> HomeFragment()
       }
    }
}