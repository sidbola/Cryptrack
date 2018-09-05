package com.sidbola.cryptrack.features.coindetails.holdings

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.database.DatabaseHandler
import com.sidbola.cryptrack.database.Transaction
import com.sidbola.cryptrack.features.shared.extensions.hideKeyboard
import com.sidbola.cryptrack.features.shared.extensions.toast
import kotlinx.android.synthetic.main.fragment_transaction_entry.*
import java.lang.Double.parseDouble
import java.util.Calendar

class TransactionEntryFragment : Fragment() {

    private var mListener: OnTransactionEntryInteractionListener? = null
    private var coinTicker: String = ""
    private var coinPrice: String = ""
    private lateinit var mDateSetListener: DatePickerDialog.OnDateSetListener

    private var transYear: Int = 0
    private var transMonth: Int = 0
    private var transDay: Int = 0

    private lateinit var db: DatabaseHandler

    private lateinit var newTransaction: Transaction

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coinTicker = arguments?.get("ticker").toString()
        coinPrice = arguments?.get("price").toString()
        coinPrice = coinPrice.drop(2)

        db = context?.let { DatabaseHandler(it) }!!

        val calendar: Calendar = Calendar.getInstance()
        transYear = calendar.get(Calendar.YEAR)
        transMonth = calendar.get(Calendar.MONTH)
        transDay = calendar.get(Calendar.DAY_OF_MONTH)

        transactionTickerLabel.text = coinTicker

        setupBuySellButtons()
        setupDateEditText()
    }

    private fun setupBuySellButtons() {
        transactionBuyButton.setOnClickListener {
            executeTransaction("Buy")
        }

        transactionSellButton.setOnClickListener {
            executeTransaction("Sell")
        }
    }

    private fun setupDateEditText() {
        transactionDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val dialog = DatePickerDialog(activity,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        transYear, transMonth, transDay)
                dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }
        }

        transactionDate.setOnClickListener {
            val dialog = DatePickerDialog(activity,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    transYear, transMonth, transDay)
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        mDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val dateAsString = monthNumberToName(month) + " " + day + " " + year

            transactionDate.setText(dateAsString, TextView.BufferType.EDITABLE)
        }
    }

    private fun executeTransaction(transactionType: String) {

        var dateAsString = monthNumberToName(transMonth) + " " + transDay + " " +
                transYear

        val price: Double

        price = if (transactionPrice.text.toString() != "" && isValidDouble(transactionPrice.text.toString())) {
            parseDouble(transactionPrice.text.toString())
        } else {
            val safeCoinPrice = coinPrice.replace(",", "")
            safeCoinPrice.toDouble()
        }

        if (transactionDate.text.toString() != "") {
            dateAsString = transactionDate.text.toString()
        }

        if (transactionQuantity.text.toString() != "" && isValidDouble(transactionQuantity.text.toString())) {
            newTransaction = Transaction(coinTicker, price, (transactionQuantity.text).toString().toDouble(), dateAsString, transactionType, 4)
            db.insertTransaction(newTransaction)

            activity?.hideKeyboard(transactionDate)
            activity?.hideKeyboard(transactionPrice)
            activity?.hideKeyboard(transactionQuantity)

            mListener?.onTransactionEntryFragmentInteraction(newTransaction)

            fragmentManager?.popBackStack()
        } else {
            activity?.toast("Amount cannot be empty")
        }
    }

    private fun isValidDouble(input: String): Boolean {
        return try {
            parseDouble(input)
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnTransactionEntryInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun monthNumberToName(monthNumber: Int): String {
        return when (monthNumber) {
            0 -> "January"
            1 -> "February"
            2 -> "March"
            3 -> "April"
            4 -> "May"
            5 -> "June"
            6 -> "July"
            7 -> "August"
            8 -> "September"
            9 -> "October"
            10 -> "November"
            11 -> "December"
            else -> "Invalid Number"
        }
    }

    interface OnTransactionEntryInteractionListener {
        fun onTransactionEntryFragmentInteraction(addedTransaction: Transaction)
    }
}
