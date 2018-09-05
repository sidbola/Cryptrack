package com.sidbola.cryptrack.features.mainscreen.discover

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.features.mainscreen.MainNavigationActivity
import com.sidbola.cryptrack.features.shared.extensions.schedule
import com.sidbola.cryptrack.features.shared.model.CoinData
import com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.hour.PerHourDataProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlinx.android.synthetic.main.item_discover_alternate.view.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class DiscoverFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var top25Adapter: DiscoverListAdapter
    private lateinit var winnersAdapter: DiscoverListAdapter
    private lateinit var losersAdapter: DiscoverListAdapter
    private var counter = 0

    private var top25List = mutableListOf<CoinData>()
    private var winnersList = mutableListOf<CoinData>()
    private var losersList = mutableListOf<CoinData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val act = activity as MainNavigationActivity
        ViewCompat.setNestedScrollingEnabled(discoverRecyclerView, false)
        ViewCompat.setNestedScrollingEnabled(topWinnersRecyclerView, false)
        ViewCompat.setNestedScrollingEnabled(topLosersRecyclerView, false)

        if (act.hasInternetConnection()) {

            top25List = act.discoverData.top100.subList(0, 25)
            winnersList = act.discoverData.topWinners.subList(0, 5)
            losersList = act.discoverData.topLosers.subList(5, 10)
            losersList.reverse()

            top25Adapter = DiscoverListAdapter(top25List, act)
            top25Adapter.itemClickStream.subscribe { v ->

                val coinToDisplay: CoinData

                for (coin in top25List) {
                    if (v.discoverTicker.text.toString() == coin.ticker) {
                        coinToDisplay = coin
                        mListener?.onDiscoverFragmentInteraction(coinToDisplay)

                        break
                    }
                }
            }

            winnersAdapter = DiscoverListAdapter(winnersList, act)
            winnersAdapter.itemClickStream.subscribe { v ->

                val coinToDisplay: CoinData

                for (coin in winnersList) {
                    if (v.discoverTicker.text.toString() == coin.ticker) {
                        coinToDisplay = coin
                        mListener?.onDiscoverFragmentInteraction(coinToDisplay)

                        break
                    }
                }
            }

            losersAdapter = DiscoverListAdapter(losersList, act)
            losersAdapter.itemClickStream.subscribe { v ->

                val coinToDisplay: CoinData

                for (coin in losersList) {
                    if (v.discoverTicker.text.toString() == coin.ticker) {
                        coinToDisplay = coin
                        mListener?.onDiscoverFragmentInteraction(coinToDisplay)

                        break
                    }
                }
            }

            topWinnersRecyclerView.layoutManager = LinearLayoutManager(activity)
            topWinnersRecyclerView.adapter = winnersAdapter

            topLosersRecyclerView.layoutManager = LinearLayoutManager(activity)
            topLosersRecyclerView.adapter = losersAdapter

            discoverRecyclerView.layoutManager = LinearLayoutManager(activity)
            discoverRecyclerView.adapter = top25Adapter

            val scheduledExecuter = Executors.newScheduledThreadPool(5)
            try {

                updateList(scheduledExecuter, winnersList, winnersAdapter)
                updateList(scheduledExecuter, losersList, losersAdapter)
                updateList(scheduledExecuter, top25List, top25Adapter)
            } finally {
                scheduledExecuter.shutdown()
            }
        } else {
        }
    }

    private fun updateList(scheduledExecuter: ScheduledExecutorService, coinList: MutableList<CoinData>, listAdapter: DiscoverListAdapter) {
        for (currentCoin in coinList) {
            scheduledExecuter.schedule(counter * 200.toLong()) {
                val historicalData = PerHourDataProvider.provideHistoricalDataPerHour()
                historicalData.getHistoricalDataPerHour(currentCoin.ticker, "USD", 24)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { result ->

                            currentCoin.perHourData = result.Data!!

                            listAdapter.notifyItemChanged(coinList.indexOf(currentCoin))
                        }
            }
            counter++
        }
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
        fun onDiscoverFragmentInteraction(coin: CoinData)
    }
}
