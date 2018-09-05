package com.sidbola.cryptrack.features.coindetails.holdings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.database.DatabaseHandler
import com.sidbola.cryptrack.database.Transaction
import com.sidbola.cryptrack.features.coindetails.CoinInfoPageFragment
import com.sidbola.cryptrack.features.shared.extensions.specialFormat
import com.sidbola.cryptrack.features.shared.model.TransactionAdapterItem
import kotlinx.android.synthetic.main.fragment_holdings.*
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast

class HoldingsFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var subCoinTicker: String = ""
    private var subCoinPrice: String = ""

    private var currentHoldingsValue = 0.0
    private var currentHoldingsAmount = 0.0

    private var totalInvestment = 0.0

    private var investmentOverallReturn = 0.0
    private var investmentPercentOverallReturn = 0.0

    private val transactionItem = TransactionAdapterItem()
    private lateinit var transactionAdapter: TransactionAdapter

    private lateinit var db: DatabaseHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_holdings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parent = parentFragment as CoinInfoPageFragment
        subCoinTicker = parent.getCoinTickerFromParent()
        subCoinPrice = parent.getCoinPriceFromParent()

        transactionItem.currentPrice = subCoinPrice.drop(2).replace(",", "").toDouble()
        transactionItem.ticker = subCoinTicker

        db = context?.let { DatabaseHandler(it) }!!

        updateHoldingsViews()

        addTransactionButton.setOnClickListener {
            if (! activity?.supportFragmentManager?.fragments?.
                    contains(activity?.supportFragmentManager?.findFragmentByTag("ADDTRANSACTION"))!!){
                val bundle = Bundle()
                bundle.putString("ticker", subCoinTicker)
                bundle.putString("price", subCoinPrice)

                val transactionFragment = TransactionEntryFragment()
                transactionFragment.arguments = bundle

                val ft = activity?.supportFragmentManager?.beginTransaction()
                ft?.setCustomAnimations(R.anim.slide_in_top,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom)
                ft?.addToBackStack(null)
                ft?.replace(R.id.transactionEntryFragmentHolder, transactionFragment, "ADDTRANSACTION")
                ft?.commit()
            }

        }
    }

    private fun removedTransaction(trans: Transaction, position: Int) {
        alert("Are you sure you want to delete this transaction?") {
            title = "Caution"
            positiveButton("Yes") {
                if (db.deleteTransaction(trans.transactionId)) {
                    updateHoldingsViews()
                    transactionItem.listOfTransactions.remove(trans)
                    transactionAdapter.notifyItemRemoved(position)
                    mListener?.onHoldingsFragmentInteraction("Update portfolio page")
                } else {
                    toast("Failed to delete")
                }
            }
            noButton { }
        }.show()
    }

    private fun updateHoldingsViews() {

        transactionItem.listOfTransactions.clear()
        currentHoldingsAmount = 0.0
        totalInvestment = 0.0

        transactionItem.listOfTransactions = db.getTransactionsForTicker(subCoinTicker) as ArrayList<Transaction>

        transactionItem.listOfTransactions = ArrayList(transactionItem.listOfTransactions.sortedWith(compareBy { it.date }))

        if (transactionItem.listOfTransactions.size > 0) {
            transactionItem.listOfTransactions.reverse()

            transactionsRecyclerView.layoutManager = LinearLayoutManager(context)
            transactionAdapter = TransactionAdapter(transactionItem) { transaction, position -> removedTransaction(transaction, position) }
            transactionsRecyclerView.adapter = transactionAdapter

            for (transaction in transactionItem.listOfTransactions) {
                if (transaction.type == "Buy") {
                    currentHoldingsAmount += transaction.amount
                    totalInvestment += transaction.amount * transaction.buyPrice
                } else {
                    currentHoldingsAmount -= transaction.amount
                    totalInvestment -= transaction.amount * transaction.buyPrice
                }
            }

            currentHoldingsValue = currentHoldingsAmount * subCoinPrice.drop(2).replace(",", "").toDouble()

            investmentOverallReturn = (currentHoldingsValue - totalInvestment)

            investmentPercentOverallReturn = (investmentOverallReturn / totalInvestment) * 100

            when {
                investmentOverallReturn > 0 -> {
                    overallReturn.setTextColor(Color.parseColor("#3ef2b0"))
                    val tempText = "$ ${investmentOverallReturn.specialFormat(2)} ↑"
                    overallReturn.text = tempText
                }
                investmentOverallReturn < 0 -> {
                    overallReturn.setTextColor(Color.parseColor("#f14d3e"))
                    val tempText = "$ ${Math.abs(investmentOverallReturn).specialFormat(2)} ↓"
                    overallReturn.text = tempText
                }
                else -> {
                    overallReturn.setTextColor(Color.parseColor("#3ef2b0"))
                    val tempText = "$ ${investmentOverallReturn.specialFormat(2)} ↑↓"
                    overallReturn.text = tempText
                }
            }

            when {
                investmentPercentOverallReturn > 0 -> {
                    percentOverallReturn.setTextColor(Color.parseColor("#3ef2b0"))
                    val tempText = "+$investmentPercentOverallReturn ↑"
                    percentOverallReturn.text = tempText
                }
                investmentPercentOverallReturn < 0 -> {
                    percentOverallReturn.setTextColor(Color.parseColor("#f14d3e"))
                    val tempText = "$investmentPercentOverallReturn ↓"
                    percentOverallReturn.text = tempText
                }
                else -> {
                    percentOverallReturn.setTextColor(Color.parseColor("#3ef2b0"))
                    val tempText = "+$investmentPercentOverallReturn ↑↓"
                    percentOverallReturn.text = tempText
                }
            }

            val stringCurrentHoldings = "$ " + currentHoldingsValue.specialFormat(2)
            val stringAmountOfTokens = currentHoldingsAmount.specialFormat(4) + " " + subCoinTicker
            val stringPercentOverallReturn = investmentPercentOverallReturn.specialFormat(3) + "%"
            val stringNetCost = "$ " + totalInvestment.specialFormat(2)

            holdingsValue.text = stringCurrentHoldings
            amountOfTokens.text = stringAmountOfTokens
            percentOverallReturn.text = stringPercentOverallReturn
            numberOfTransactions.text = transactionItem.listOfTransactions.size.toString()

            netCost.text = stringNetCost
        } else {

            val zeroHoldings = "$ 00.00"
            val zeroTokens = "000.00 Tokens"
            val zeroReturn = "0.00%"
            val zeroNetCost = "$0.00"

            holdingsValue.text = zeroHoldings
            amountOfTokens.text = zeroTokens
            percentOverallReturn.text = zeroReturn
            numberOfTransactions.text = "0"
            netCost.text = zeroNetCost
            overallReturn.text = zeroNetCost
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

    fun newTransactionEntered() {
        updateHoldingsViews()
        transactionsRecyclerView.scrollToPosition(0)
    }

    interface OnFragmentInteractionListener {
        fun onHoldingsFragmentInteraction(response: String)
    }
} // Required empty public constructor