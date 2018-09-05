package com.sidbola.cryptrack.features.mainscreen.portfolio

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
import com.sidbola.cryptrack.database.DatabaseHandler
import com.sidbola.cryptrack.features.shared.extensions.specialFormat
import com.sidbola.cryptrack.features.shared.model.CoinData
import com.sidbola.cryptrack.features.shared.view.CoinGraphDataScrubber
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_portfolio.view.*

class PortfolioListAdapter(
    passedCoinList: MutableList<CoinData>,
    var context: Context
)
    : RecyclerView.Adapter<PortfolioListAdapter.PortfolioItemViewHolder>() {

    private var coinList: MutableList<CoinData> = passedCoinList

    private var db: DatabaseHandler = DatabaseHandler(context)

    val itemClickStream: PublishSubject<View> = PublishSubject.create()

    override fun onBindViewHolder(holder: PortfolioItemViewHolder, position: Int) {
        val currentCoin = coinList[position]

        val totalHoldings: Double
        var amountHeld = 0.0

        val transactionList = db.getTransactionsForTicker(currentCoin.ticker)

        for (transaction in transactionList) {
            if (transaction.type == "Buy") {
                amountHeld += transaction.amount
            } else {
                amountHeld -= transaction.amount
            }
        }

        totalHoldings = amountHeld * currentCoin.price.replace(",", "").drop(2).toDouble()

        val stringPortfolioItemTotalHoldings = "$ " + totalHoldings.specialFormat(2)
        val stringPortfolioItemAmountHeld = amountHeld.specialFormat(3) + " " + currentCoin.ticker

        holder.view.portfolioItemTotalHoldings?.text = stringPortfolioItemTotalHoldings
        holder.view.portfolioItemAmountHeld?.text = stringPortfolioItemAmountHeld

        setFadeAnimation(holder.view.portfolioGraph!!)
        setFadeAnimation(holder.view.portfolioItemCoinPercentChange!!)

        TextViewCompat.setAutoSizeTextTypeWithDefaults(holder.view.portfolioItemCoinPercentChange, TypedValue.COMPLEX_UNIT_DIP)

        holder.view.portfolioItemCoinName?.text = currentCoin.name
        holder.view.portfolioItemCoinPrice?.text = currentCoin.price
        holder.view.portfolioItemTicker?.text = currentCoin.ticker

        var formattedChange: String

        currentCoin.percentChange = currentCoin.percentChange.replace(",", "")

        if (currentCoin.percentChange.toFloat() > 0) {
            holder.view.portfolioItemCoinPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            formattedChange = "+" + currentCoin.percentChange + "% ↑"
            holder.view.portfolioItemCoinPercentChange?.text = formattedChange
        } else {
            holder.view.portfolioItemCoinPercentChange?.setTextColor(Color.parseColor("#f14d3e"))
            formattedChange = currentCoin.percentChange + "% ↓"
            holder.view.portfolioItemCoinPercentChange?.text = formattedChange
        }
        if (currentCoin.percentChange.toFloat() == 0f) {
            holder.view.portfolioItemCoinPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            formattedChange = "+" + currentCoin.percentChange + "% ↑↓"
            holder.view.portfolioItemCoinPercentChange?.text = formattedChange
        }

        if (currentCoin.perHourData!!.size != 0) {
            val graphData = CoinGraphDataScrubber()
                    .scrubData(currentCoin.perHourData!!)

            if (currentCoin.perHourData!!.size > 0) {
                holder.view.portfolioGraph?.setDataPoints(graphData)
            }

            if (graphData.changePositive) {
                holder.view.portfolioItemCoinPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            } else {
                holder.view.portfolioItemCoinPercentChange?.setTextColor(Color.parseColor("#f14d3e"))
            }
            if (graphData.percentChange == 0.0f) {
                holder.view.portfolioItemCoinPercentChange?.setTextColor(Color.parseColor("#3ef2b0"))
            }

            holder.view.portfolioItemCoinPercentChange?.text = graphData.percentChangeString
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val coinItem = layoutInflater.inflate(R.layout.item_portfolio, parent, false)
        return PortfolioItemViewHolder(coinItem)
    }

    override fun getItemCount(): Int {
        return coinList.size
    }

    private fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 500
        view.startAnimation(anim)
    }

    inner class PortfolioItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener { v -> itemClickStream.onNext(v) }
        }
    }
}