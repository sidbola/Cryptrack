package com.sidbola.cryptrack.features.coindetails

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.features.shared.model.CoinDataSnapshot
import com.sidbola.cryptrack.features.shared.model.HistoricalDataResponse
import com.sidbola.cryptrack.features.shared.view.CoinGraphDataScrubber
import com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.day.PerDayDataProvider
import com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.hour.PerHourDataProvider
import com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.minute.PerMinuteDataProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_coin_info_page.*
import kotlinx.android.synthetic.main.fragment_coin_snapshot.*
import org.jetbrains.anko.displayMetrics
import java.util.HashMap

const val BY_MINUTE = 0
const val BY_HOUR = 1
const val BY_DAY = 2
const val ALL_TIME = 3

const val DATA_1H = 0
const val DATA_24H = 1
const val DATA_5D = 2
const val DATA_1M = 3
const val DATA_3M = 4
const val DATA_6M = 5
const val DATA_1Y = 6
const val DATA_ALL = 7

class SnapshotFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    private var subCoinTicker = ""
    private var subCoinMc = ""
    private var subCoinOpen = ""
    private var subCoinLow = ""
    private var subCoinHigh = ""
    private var subCoinVolume = ""

    private var currentGraph = 1

    @SuppressLint("UseSparseArrays")
    private val historicalDataCache = HashMap<Int, ArrayList<CoinDataSnapshot>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_coin_snapshot, container, false)

        val parent = parentFragment as CoinInfoPageFragment

        subCoinTicker = parent.getCoinTickerFromParent()
        subCoinMc = parent.getCoinMcFromParent()
        subCoinOpen = parent.getCoinOpenFromParent()
        subCoinLow = parent.getCoinLowFromParent()
        subCoinHigh = parent.getCoinHighFromParent()
        subCoinVolume = parent.getCoinVolumeFromParent()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleProgressBar(true)

        val parent = parentFragment as CoinInfoPageFragment

        snapshotMarketcap.text = subCoinMc
        snapshotOpen.text = subCoinOpen
        snapshotLow.text = subCoinLow
        snapshotHigh.text = subCoinHigh

        getGraphData(subCoinTicker, BY_MINUTE, 60)?.subscribe {
            saveHistoricalData(it, 1, DATA_1H)
            try {
                if (currentGraph == DATA_1H) {
                    load1HGraph()
                }
            } catch (e: Exception) {
            }
        }

        getGraphData(subCoinTicker, BY_MINUTE, 24*60)?.subscribe {
            saveHistoricalData(it, 12, DATA_24H)
            try {
                if (currentGraph == DATA_24H) {
                    load24HGraph()
                }
            } catch (e: Exception) {
            }
        }

        getGraphData(subCoinTicker, BY_HOUR, 24*5)?.subscribe {
            saveHistoricalData(it, 1, DATA_5D)
            try {
                if (currentGraph == DATA_5D) {
                    load5DGraph()
                }
            } catch (e: Exception) {
            }
        }

        getGraphData(subCoinTicker, BY_HOUR, 24*30)?.subscribe {
            saveHistoricalData(it, 6, DATA_1M)
            try {
                if (currentGraph == DATA_1M) {
                    load1MGraph()
                }
            } catch (e: Exception) {
            }
        }

        getGraphData(subCoinTicker, BY_DAY, 30*3)?.subscribe {
            saveHistoricalData(it, 1, DATA_3M)
            try {
                if (currentGraph == DATA_3M) {
                    load3MGraph()
                }
            } catch (e: Exception) {
            }
        }

        getGraphData(subCoinTicker, BY_DAY, 30*6)?.subscribe {
            saveHistoricalData(it, 2, DATA_6M)
            try {
                if (currentGraph == DATA_6M) {
                    load6MGraph()
                }
            } catch (e: Exception) {
            }
        }

        getGraphData(subCoinTicker, BY_DAY, 360)?.subscribe {
            saveHistoricalData(it, 3, DATA_1Y)
            try {
                if (currentGraph == DATA_1Y) {
                    load1YGraph()
                }
            } catch (e: Exception) {
            }
        }

        getGraphData(subCoinTicker, ALL_TIME, 0)?.subscribe {
            saveHistoricalData(it, 0, DATA_ALL)
            try {
                if (currentGraph == DATA_ALL) {
                    loadALLGraph()
                }
            } catch (e: Exception) {
            }
        }

        val stringFullInfoChange = "0.00" + "  [ " + "0.00" + "% ]"

        fullInfoPrice.text = parent.coinPrice
        fullInfoChange.text = stringFullInfoChange

        if (parent.coinPercentChange.toFloat() > 0) {
            fullInfoChange.setTextColor(Color.parseColor("#3ef2b0"))
        } else {
            fullInfoChange.setTextColor(Color.parseColor("#f14d3e"))
        }

        fullCoinSnapshot.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                parent.coinInfoViewPager.swipeEnabled(false)
            } else if (event.action == MotionEvent.ACTION_UP) {
                parent.coinInfoViewPager.swipeEnabled(true)
            }

            false
        }

        button1h.setOnClickListener {
            load1HGraph()
        }

        button24h.setOnClickListener {
            load24HGraph()
        }

        button5d.setOnClickListener {
            load5DGraph()
        }

        button1m.setOnClickListener {
            load1MGraph()
        }

        button3m.setOnClickListener {
            load3MGraph()
        }

        button6m.setOnClickListener {
            load6MGraph()
        }

        button1y.setOnClickListener {
            load1YGraph()
        }

        buttonAll.setOnClickListener {
            loadALLGraph()
        }


        val height = activity?.displayMetrics?.heightPixels

        if (height != null) {
            when {
                height > 1501 -> {
                    // Do nothing I guess
                }
                height in 700..1500 -> {
                    val constraint = ConstraintSet()
                    constraint.clone(constraintLayout)
                    constraint.setDimensionRatio(graphHolder.id, "1:0.8")
                    constraint.applyTo(constraintLayout)
                    fullInfoPrice.textSize = 32f
                    fullInfoChange.textSize = 13f

                }
                else -> {
                    val constraint = ConstraintSet()
                    constraint.clone(constraintLayout)
                    constraint.setDimensionRatio(graphHolder.id, "1:0.79")
                    constraint.applyTo(constraintLayout)
                    fullInfoPrice.textSize = 25f
                    fullInfoChange.textSize = 12f

                }
            }

        }
    }

    private fun load1HGraph() {
        currentGraph = DATA_1H

        toggleProgressBar(true)
        highlightText(button1h)
        if (historicalDataCache.containsKey(DATA_1H)) {
            historicalDataCache[DATA_1H]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun load24HGraph() {
        currentGraph = DATA_24H

        toggleProgressBar(true)
        highlightText(button24h)
        if (historicalDataCache.containsKey(DATA_24H)) {
            historicalDataCache[DATA_24H]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun load5DGraph() {
        currentGraph = DATA_5D

        toggleProgressBar(true)
        highlightText(button5d)
        if (historicalDataCache.containsKey(DATA_5D)) {
            historicalDataCache[DATA_5D]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun load1MGraph() {
        currentGraph = DATA_1M

        toggleProgressBar(true)
        highlightText(button1m)
        if (historicalDataCache.containsKey(DATA_1M)) {
            historicalDataCache[DATA_1M]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun load3MGraph() {
        currentGraph = DATA_3M

        toggleProgressBar(true)
        highlightText(button3m)
        if (historicalDataCache.containsKey(DATA_3M)) {
            historicalDataCache[DATA_3M]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun load6MGraph() {
        currentGraph = DATA_6M

        toggleProgressBar(true)
        highlightText(button6m)
        if (historicalDataCache.containsKey(DATA_6M)) {
            historicalDataCache[DATA_6M]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun load1YGraph() {
        currentGraph = DATA_1Y

        toggleProgressBar(true)
        highlightText(button1y)
        if (historicalDataCache.containsKey(DATA_1Y)) {
            historicalDataCache[DATA_1Y]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun loadALLGraph() {
        currentGraph = DATA_ALL

        toggleProgressBar(true)
        highlightText(buttonAll)
        if (historicalDataCache.containsKey(DATA_ALL)) {
            historicalDataCache[DATA_ALL]?.let { it1 -> setGraph(it1) }
        }
    }

    private fun getGraphData(
        ticker: String,
        dataType: Int,
        limit: Int
    ): Observable<HistoricalDataResponse>? {

        when (dataType) {
            BY_MINUTE -> {

                val historicalData = PerMinuteDataProvider.provideHistoricalDataPerMinute()
                return historicalData.getHistoricalDataPerMinute(ticker, "USD", limit)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .onErrorReturn { null }
            }

            BY_HOUR -> {
                val historicalData = PerHourDataProvider.provideHistoricalDataPerHour()
                return historicalData.getHistoricalDataPerHour(ticker, "USD", limit)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
            }

            BY_DAY -> {
                val historicalData = PerDayDataProvider.provideHistoricalDataPerDay()
                return historicalData.getHistoricalDataPerDay(ticker, "USD", limit)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
            }

            ALL_TIME -> {
                val historicalData = PerDayDataProvider.provideHistoricalDataPerDay()
                return historicalData.getHistoricalDataPerDay(ticker, "USD", limit, "true")
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
            }

            else -> {
                return null
            }
        }
    }

    private fun saveHistoricalData(historicalDataResponse: HistoricalDataResponse, interval: Int, resultType: Int) {
        val dataSet = ArrayList<CoinDataSnapshot>()

        if (resultType != DATA_ALL) {
            for ((currentElement, data) in historicalDataResponse.Data!!.withIndex()) {
                if (currentElement % interval == 0) {
                    dataSet.add(data)
                }
            }

            historicalDataCache[resultType] = dataSet
        } else {
            val skipBy = historicalDataResponse.Data!!.size / 150

            if (skipBy != 0) {
                for ((currentElement, data) in historicalDataResponse.Data.withIndex()) {
                    if (currentElement % skipBy == 0) {
                        dataSet.add(data)
                    }
                }
            }

            historicalDataCache[resultType] = dataSet
        }
    }

    private fun highlightText(view: View) {
        removeHighLightFromText()
        activity?.findViewById<TextView>(view.id)?.setTextColor(Color.WHITE)
    }

    private fun removeHighLightFromText() {
        button1h.setTextColor(Color.parseColor("#4c4c4c"))
        button24h.setTextColor(Color.parseColor("#4c4c4c"))
        button5d.setTextColor(Color.parseColor("#4c4c4c"))
        button1m.setTextColor(Color.parseColor("#4c4c4c"))
        button3m.setTextColor(Color.parseColor("#4c4c4c"))
        button6m.setTextColor(Color.parseColor("#4c4c4c"))
        button1y.setTextColor(Color.parseColor("#4c4c4c"))
        buttonAll.setTextColor(Color.parseColor("#4c4c4c"))
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

    private fun toggleProgressBar(isOn: Boolean) {
        try {
            if (isOn) {
                graphLoading.visibility = View.VISIBLE
            } else {
                graphLoading.visibility = View.GONE
            }
        } catch (e: Exception) {
        }
    }

    private fun setGraph(dataSet: ArrayList<CoinDataSnapshot>) {
        if (dataSet.size != 0) {

            val graphData = CoinGraphDataScrubber()
                    .scrubData(dataSet)

            try {

                if (dataSet.size > 0) {
                    fullCoinSnapshot?.setDataPoints(graphData)
                }

                if (graphData.changePositive) {
                    fullInfoChange?.setTextColor(Color.parseColor("#3ef2b0"))
                } else {
                    fullInfoChange?.setTextColor(Color.parseColor("#f14d3e"))
                }

                val stringFullInfoChange = graphData.dollarChange.toString() + "  [ " +
                    graphData.percentChangeString + " ]"

                fullInfoChange.text = stringFullInfoChange

                toggleProgressBar(false)
            } catch (e: Exception) {
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        fun onCoinSnapshotFragmentInteraction(response: String)
    }
} // Required empty public constructor
