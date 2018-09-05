package com.sidbola.cryptrack.features.mainscreen.watchlist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.database.DatabaseHandler
import com.sidbola.cryptrack.features.mainscreen.MainNavigationActivity
import com.sidbola.cryptrack.features.mainscreen.discover.DiscoverListAdapter
import com.sidbola.cryptrack.features.shared.extensions.schedule
import com.sidbola.cryptrack.features.shared.extensions.toast
import com.sidbola.cryptrack.features.shared.model.CoinData
import com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.hour.PerHourDataProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.android.synthetic.main.watchlist_item.view.*
import java.util.concurrent.Executors

class WatchlistFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var mDatabase: DatabaseReference

    lateinit var coinList: MutableList<CoinData>
    lateinit var listAdapter: DiscoverListAdapter

    var counter = 0

    private lateinit var db: DatabaseHandler
    private lateinit var watchlistTickers: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainNavigationActivity

        ViewCompat.setNestedScrollingEnabled(watchlistRecyclerView, false)

        // TextViewCompat.setAutoSizeTextTypeWithDefaults(watchlistPageTitle, TypedValue.COMPLEX_UNIT_DIP)

        if (act.hasInternetConnection()) {

            coinList = mutableListOf()
            listAdapter = DiscoverListAdapter(coinList, act)

            initializeList()

            listAdapter.itemClickStream.subscribe { v ->

                val coinToDisplay: CoinData

                for (coin in coinList) {
                    if (v.discoverTicker.text.toString() == coin.ticker) {
                        coinToDisplay = coin
                        mListener?.onWatchlistFragmentInteraction(coinToDisplay)
                        break
                    }
                }
            }
            watchlistRecyclerView.layoutManager = LinearLayoutManager(activity)
            watchlistRecyclerView.adapter = listAdapter
        } else {
            // tempText.text = "Watchlist feature only available with Internet Connection. Please try again later."
        }
    }

    fun listChanged(ticker: String, didAdd: Boolean) {
        if (didAdd) {
            updateWatchlistItem(ticker)
        } else {
            var index = -1
            for (coin in coinList) {
                if (coin.ticker == ticker) {
                    index = coinList.indexOf(coin)
                }
            }
            if (index > 0) {
                listAdapter.notifyItemRemoved(index)
                coinList.removeAt(index)
            }
        }
    }

    private fun initializeList() {
        db = context?.let { DatabaseHandler(it) }!!
        watchlistTickers = db.getAllWatchedCoinTickers()
        coinList.clear()

        for (ticker in watchlistTickers) {
            updateWatchlistItem(ticker)
        }
    }

    private fun updateWatchlistItem(ticker: String) {

        // TODO: FIX ALL OF THIS LOGIC - DO IT BETTER

        mDatabase = FirebaseDatabase.getInstance().reference.child("allCoins").child(ticker)

        val coinListListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                activity?.toast("Listener Cancelled")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val mRaw: Any

                mRaw = when {
                    p0.child("marketCapRaw").value is Long -> (p0.child("marketCapRaw").value as Long).toFloat()
                    p0.child("marketCapRaw").value is Double -> (p0.child("marketCapRaw").value as Double).toFloat()
                    p0.child("marketCapRaw").value is Float -> p0.child("marketCapRaw").value as Float
                    p0.child("marketCapRaw").value != null -> p0.child("marketCapRaw").value as Float
                    else -> 0.0f
                }

                if (p0.child("high24hr").value != null) {
                    val coin = CoinData(
                            p0.child("high24hr").value as String,
                            p0.child("low24hr").value as String,
                            p0.child("marketCapDisplay").value as String,
                            mRaw,
                            p0.child("name").value as String,
                            p0.child("open24hr").value as String,
                            p0.child("percentChange").value as String,
                            p0.child("price").value as String,
                            p0.child("supply").value as String,
                            p0.child("ticker").value as String,
                            p0.child("volume24hr").value as String,
                            ArrayList()
                    )

                    coinList.add(coin)
                    listAdapter.notifyItemInserted(coinList.indexOf(coin))

                    val scheduledExecuter = Executors.newScheduledThreadPool(1)
                    try {
                            scheduledExecuter.schedule(counter * 200.toLong()) {
                                val historicalData = PerHourDataProvider.provideHistoricalDataPerHour()
                                historicalData.getHistoricalDataPerHour(coin.ticker, "USD", 24)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe { result ->

                                            coin.perHourData = result.Data!!

                                            listAdapter.notifyItemChanged(coinList.indexOf(coin))
                                        }
                            }
                            counter++
                    } finally {
                        scheduledExecuter.shutdown()
                    }
                }
            }
        }
        mDatabase.addListenerForSingleValueEvent(coinListListener)
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
        fun onWatchlistFragmentInteraction(coin: CoinData)
    }
}
