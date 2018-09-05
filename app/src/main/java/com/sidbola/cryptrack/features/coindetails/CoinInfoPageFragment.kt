package com.sidbola.cryptrack.features.coindetails

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.TextViewCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.database.DatabaseHandler
import com.sidbola.cryptrack.features.mainscreen.MainNavigationActivity
import kotlinx.android.synthetic.main.activity_main_navigation.*
import kotlinx.android.synthetic.main.fragment_coin_info_page.*

class CoinInfoPageFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var mCoinInfoPageAdapter: CoinInfoPageAdapter? = null
    private var coinTicker = ""
    private var coinName = ""
    private var coinMc = ""
    private var coinOpen = ""
    private var coinLow = ""
    private var coinHigh = ""
    private var coinVolume = ""
    var coinPercentChange = ""
    var coinPrice = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_coin_info_page, container, false)

        coinTicker = arguments?.get("ticker").toString()
        coinName = arguments?.get("name").toString()
        coinMc = arguments?.get("mc").toString()
        coinOpen = arguments?.get("open").toString()
        coinLow = arguments?.get("low").toString()
        coinHigh = arguments?.get("high").toString()
        coinVolume = arguments?.get("volume").toString()
        coinPercentChange = arguments?.get("percentChange").toString()
        coinPrice = arguments?.get("price").toString()

        return view
    }

    fun getCoinTickerFromParent(): String {
        return coinTicker
    }

    fun getCoinNameFromParent(): String {
        return coinName
    }

    fun getCoinOpenFromParent(): String {
        return coinOpen
    }

    fun getCoinLowFromParent(): String {
        return coinLow
    }

    fun getCoinHighFromParent(): String {
        return coinHigh
    }

    fun getCoinVolumeFromParent(): String {
        return coinVolume
    }

    fun getCoinMcFromParent(): String {
        return coinMc
    }

    fun getCoinPriceFromParent(): String {
        return coinPrice
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TextViewCompat.setAutoSizeTextTypeWithDefaults(fullInfoName, TypedValue.COMPLEX_UNIT_DIP)
        TextViewCompat.setAutoSizeTextTypeWithDefaults(fullInfoTicker, TypedValue.COMPLEX_UNIT_DIP)

        fullInfoName.text = coinName
        fullInfoTicker.text = coinTicker

        mCoinInfoPageAdapter = context?.let { CoinInfoPageAdapter(childFragmentManager) }

        coinInfoViewPager?.offscreenPageLimit = 2

        coinInfoViewPager?.adapter = mCoinInfoPageAdapter

        coinInfoTabs.setupWithViewPager(coinInfoViewPager)

        coinInfoTabs.getTabAt(0)!!.text = "Snapshot"
        coinInfoTabs.getTabAt(1)!!.text = "Holdings"
        coinInfoTabs.getTabAt(2)!!.text = "News"

        coinInfoBackButton.setOnClickListener { fragmentManager?.popBackStack() }

        val db = context?.let { DatabaseHandler(it) }
        var watchlistStatus = db?.getWatchStatus(coinTicker)
        if (watchlistStatus == 0) {
            watchlistButton.setImageResource(R.drawable.ic_watchlist_unselected)
        } else {
            watchlistButton.setImageResource(R.drawable.ic_watchlist)
        }

        watchlistButton.setOnClickListener {
            watchlistStatus = db?.getWatchStatus(coinTicker)
            db?.changeWatchStatus(coinTicker)

            if (watchlistStatus == 0) {
                watchlistButton.setImageResource(R.drawable.ic_watchlist)
                mListener?.onCoinInfoPageFragmentInteraction(coinTicker, true)
//                Snackbar.make(coinInfoPage,"$coinTicker added",Snackbar.LENGTH_LONG).setAction("Undo") {
//                    watchlistButton.setImageResource(R.drawable.ic_watchlist_unselected)
//                    mListener?.onCoinInfoPageFragmentInteraction(coinTicker, false)
//                }.show()
            } else {
                watchlistButton.setImageResource(R.drawable.ic_watchlist_unselected)
                mListener?.onCoinInfoPageFragmentInteraction(coinTicker, false)
//                Snackbar.make(coinInfoPage,"$coinTicker removed",Snackbar.LENGTH_LONG).setAction("Undo") {
//                    watchlistButton.setImageResource(R.drawable.ic_watchlist_unselected)
//                    mListener?.onCoinInfoPageFragmentInteraction(coinTicker, false)
//                }.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val act = activity as MainNavigationActivity
        act.setUiEnabled(true, act.topLevelFragmentContainer)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onCoinInfoPageFragmentInteraction(ticker: String, didAdd: Boolean)
    }
}