package com.sidbola.cryptrack.features.mainscreen.discover

import android.content.Context
import android.graphics.Color
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.features.shared.model.CoinData
import com.sidbola.cryptrack.features.shared.view.CoinGraphDataScrubber
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.watchlist_item.view.*

class DiscoverListAdapter(
    passedCoinList: MutableList<CoinData>,
    var context: Context
)
    : RecyclerView.Adapter<DiscoverListAdapter.DiscoverViewHolder>() {

    private var coinList: MutableList<CoinData> = passedCoinList

    val itemClickStream: PublishSubject<View> = PublishSubject.create()

    override fun onBindViewHolder(holder: DiscoverViewHolder, position: Int) {
        val currentCoin = coinList[position]

        setFadeAnimation(holder.view.discoverGraph!!)
        setFadeAnimation(holder.view.discoverPercentChange!!)

        TextViewCompat.setAutoSizeTextTypeWithDefaults(holder.view.discoverPercentChange, TypedValue.COMPLEX_UNIT_DIP)

        holder.view.discoverName?.text = currentCoin.name
        holder.view.discoverValue?.text = currentCoin.price.drop(2)
        holder.view.discoverTicker?.text = currentCoin.ticker

        holder.view.discoverMc?.text = currentCoin.marketCapDisplay
        var formattedChange: String

        currentCoin.percentChange = currentCoin.percentChange.replace(",", "")

        if (currentCoin.percentChange.toFloat() > 0) {
            holder.view.discoverPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            formattedChange = "+" + currentCoin.percentChange + "% ↑"
            holder.view.discoverPercentChange?.text = formattedChange
        } else {
            holder.view.discoverPercentChange?.setTextColor(Color.parseColor("#f14d3e"))
            formattedChange = currentCoin.percentChange + "% ↓"
            holder.view.discoverPercentChange?.text = formattedChange
        }
        if (currentCoin.percentChange.toFloat() == 0f) {
            holder.view.discoverPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            formattedChange = "+" + currentCoin.percentChange + "% ↑↓"
            holder.view.discoverPercentChange?.text = formattedChange
        }

        if (currentCoin.perHourData!!.size != 0) {
            val graphData = CoinGraphDataScrubber()
                    .scrubData(currentCoin.perHourData!!)

            if (currentCoin.perHourData!!.size > 0) {
                holder.view.discoverGraph?.setDataPoints(graphData)
            }

            if (graphData.changePositive) {
                holder.view.discoverPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            } else {
                holder.view.discoverPercentChange?.setTextColor(Color.parseColor("#f14d3e"))
            }
            if (graphData.percentChange == 0.0f) {
                holder.view.discoverPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            }

            holder.view.discoverPercentChange?.text = graphData.percentChangeString
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val coinItem = layoutInflater.inflate(R.layout.item_discover_alternate, parent, false)
        return DiscoverViewHolder(coinItem)
    }

    override fun getItemCount(): Int {
        return coinList.size
    }

    private fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 500
        view.startAnimation(anim)
    }

    inner class DiscoverViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener { v -> itemClickStream.onNext(v) }
        }
    }
}
