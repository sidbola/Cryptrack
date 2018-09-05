package com.sidbola.cryptrack.features.coindetails.holdings

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.database.Transaction
import com.sidbola.cryptrack.features.shared.extensions.specialFormat
import com.sidbola.cryptrack.features.shared.model.TransactionAdapterItem
import kotlinx.android.synthetic.main.item_transaction.view.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class TransactionAdapter(transactionItem: TransactionAdapterItem, private var removedListener: (Transaction, Int) -> Unit) : RecyclerView.Adapter<TransactionViewHolder>() {

    private val transactionList = transactionItem.listOfTransactions
    val ticker = transactionItem.ticker
    private val currentPrice = transactionItem.currentPrice

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val trans = transactionList[position]

        holder.bind(trans, position, removedListener)

        val roundedBuySellWorth = (trans.amount * trans.buyPrice)
        val roundedCurrentWorth = (trans.amount * currentPrice)

        val dollarProfitLoss = (roundedCurrentWorth - roundedBuySellWorth)
        val percentProfitLoss = ((roundedCurrentWorth / roundedBuySellWorth - 1) * 1000).roundToInt() / 10.0

        val stringTransProfitLoss: String

        val stringTransItemAmount = if (trans.type == "Buy") {
            "Bought " + trans.amount.specialFormat(3) + " " + trans.ticker
        } else {
            "Sold " + trans.amount.specialFormat(3) + " " + trans.ticker
        }

        val stringTransAmountInvested = if (trans.type == "Buy") {
            "Bought for $ " + roundedBuySellWorth.specialFormat(2)
        } else {
            "Sold for $ " + roundedBuySellWorth.specialFormat(2)
        }

        val stringTransPriceDuringPurchase = if (trans.type == "Buy") {
            "at $ ${trans.buyPrice.specialFormat(2)} per ${trans.ticker}"
        } else {
            "Now worth $ ${roundedCurrentWorth.specialFormat(2)}"
        }

        val stringTransItemCurrentWorth = "Now worth $ ${roundedCurrentWorth.specialFormat(2)}"

        if (dollarProfitLoss <= 0) {
            stringTransProfitLoss = if (trans.type == "Buy") {
                holder.cellView.transProfitLoss?.setTextColor(Color.parseColor("#f14d3e"))
                "${dollarProfitLoss.specialFormat(2)} (${percentProfitLoss.specialFormat(3)}%)"
            } else {
                holder.cellView.transProfitLoss?.setTextColor(Color.parseColor("#3ef2b0"))
                "+${dollarProfitLoss.absoluteValue.specialFormat(2)} (+${percentProfitLoss.absoluteValue.specialFormat(3)}%)"
            }
        } else {
            stringTransProfitLoss = if (trans.type == "Buy") {
                holder.cellView.transProfitLoss?.setTextColor(Color.parseColor("#3ef2b0"))
                "+${dollarProfitLoss.specialFormat(2)} (+${percentProfitLoss.specialFormat(3)}%)"
            } else {
                holder.cellView.transProfitLoss?.setTextColor(Color.parseColor("#f14d3e"))
                "-${dollarProfitLoss.specialFormat(2)} (-${percentProfitLoss.specialFormat(3)}%)"
            }
        }

        holder.cellView.transItemAmount?.text = stringTransItemAmount
        holder.cellView.transAmountInvested?.text = stringTransAmountInvested
        holder.cellView.transPriceDuringPurchase?.text = stringTransPriceDuringPurchase
        holder.cellView.transItemCurrentWorth?.text = stringTransItemCurrentWorth
        holder.cellView.transProfitLoss?.text = stringTransProfitLoss

        holder.cellView.transDate?.text = trans.date
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}

class TransactionViewHolder(val cellView: View) : RecyclerView.ViewHolder(cellView) {
    fun bind(trans: Transaction, position: Int, removedListener: (Transaction, Int) -> Unit) {
        cellView.removeItem.setOnClickListener { removedListener(trans, position) }
    }
}
