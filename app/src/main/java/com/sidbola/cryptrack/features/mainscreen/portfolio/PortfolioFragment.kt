package com.sidbola.cryptrack.features.mainscreen.portfolio

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.sidbola.cryptrack.database.Transaction
import com.sidbola.cryptrack.features.mainscreen.MainNavigationActivity
import com.sidbola.cryptrack.features.shared.extensions.schedule
import com.sidbola.cryptrack.features.shared.extensions.specialFormat
import com.sidbola.cryptrack.features.shared.extensions.toast
import com.sidbola.cryptrack.features.shared.model.CoinData
import com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.hour.PerHourDataProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_portfolio.*
import kotlinx.android.synthetic.main.item_portfolio.view.*
import java.util.concurrent.Executors

class PortfolioFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var mDatabase: DatabaseReference

    lateinit var coinList: MutableList<CoinData>
    lateinit var listAdapter: PortfolioListAdapter

    private lateinit var listOfTransactions: MutableList<Transaction>

    var counter = 0
    lateinit var db: DatabaseHandler
    private lateinit var portfolioTickers: MutableList<String>

    var totalHoldings = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_portfolio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TextViewCompat.setAutoSizeTextTypeWithDefaults(portfolioPageTitle,TypedValue.COMPLEX_UNIT_DIP)

        db = context?.let { DatabaseHandler(it) }!!

        if ((activity as MainNavigationActivity).hasInternetConnection()) {

            coinList = mutableListOf()
            portfolioTickers = mutableListOf()

            listOfTransactions = db.getAllTransactions()
            listAdapter = PortfolioListAdapter(coinList, activity as MainNavigationActivity)

            for (transaction in listOfTransactions) {
                portfolioTickers.add(transaction.ticker)
            }

            if (portfolioTickers.size > 0) {
                portfolioTickers = portfolioTickers.distinct() as MutableList<String>
            }

            settingsButton.setOnClickListener {
                activity?.setTheme(R.style.AppTheme)
            }

            listAdapter.itemClickStream.subscribe { v ->
                val coinToDisplay = coinList.find { coinData -> coinData.ticker == v.portfolioItemTicker.text.toString() }
                coinToDisplay?.let { mListener?.onPortfolioFragmentInteraction(it) }
            }

            for (ticker in portfolioTickers) {
                updatePortfolioItem(ticker)
            }

            portfolioRecyclerView.layoutManager = LinearLayoutManager(activity)
            portfolioRecyclerView.adapter = listAdapter
        }
    }

    fun updateList() {
        coinList.clear()
        listAdapter.notifyDataSetChanged()

        portfolioTickers = ArrayList()

        totalHoldings = 0.0

        listOfTransactions = db.getAllTransactions()

        for (transaction in listOfTransactions) {
            portfolioTickers.add(transaction.ticker)
        }

        if (portfolioTickers.size > 0) {
            portfolioTickers = portfolioTickers.distinct() as MutableList<String>

            for (ticker in portfolioTickers) {
                updatePortfolioItem(ticker)
            }
        }
    }

    private fun updatePortfolioItem(ticker: String) {
        mDatabase = FirebaseDatabase.getInstance().reference.child("allCoins").child(ticker)

        // TODO: FIX ALL OF THIS LOGIC - DO IT BETTER

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

                    val transactionList = db.getTransactionsForTicker(coin.ticker)

                    var totalHeld = 0.0

                    for (transaction in transactionList) {
                        if (transaction.type == "Buy") {
                            totalHeld += transaction.amount
                        } else {
                            totalHeld -= transaction.amount
                        }
                    }

                    totalHoldings += totalHeld * coin.price.drop(2).replace(",", "").toDouble()

                    val stringTotalHoldings = "$ ${totalHoldings.specialFormat(2)}"
                    portfolioPageTitle.text = stringTotalHoldings

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
        fun onPortfolioFragmentInteraction(coinToDisplay: CoinData)
    }
}
