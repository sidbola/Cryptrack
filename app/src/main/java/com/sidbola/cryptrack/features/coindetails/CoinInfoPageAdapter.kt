package com.sidbola.cryptrack.features.coindetails

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.sidbola.cryptrack.features.coindetails.holdings.HoldingsFragment
import com.sidbola.cryptrack.features.coindetails.news.NewsFragment

class CoinInfoPageAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SnapshotFragment()
            1 -> HoldingsFragment()
            2 -> NewsFragment()
            else -> SnapshotFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }
}