package com.sidbola.cryptrack.features.mainscreen

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.sidbola.cryptrack.features.mainscreen.discover.DiscoverFragment
import com.sidbola.cryptrack.features.mainscreen.portfolio.PortfolioFragment
import com.sidbola.cryptrack.features.mainscreen.watchlist.WatchlistFragment

class TopLevelPageAdapter(fragmentManger: FragmentManager) : FragmentPagerAdapter(fragmentManger) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> WatchlistFragment()
            1 -> PortfolioFragment()
            2 -> DiscoverFragment()
            else -> WatchlistFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }
}