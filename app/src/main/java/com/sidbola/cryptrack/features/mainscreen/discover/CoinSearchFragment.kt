package com.sidbola.cryptrack.features.mainscreen.discover

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.database.DatabaseHandler
import com.sidbola.cryptrack.features.authentication.initialsetup.USER_PREFERENCES_FILENAME
import com.sidbola.cryptrack.features.shared.extensions.hideKeyboard
import com.sidbola.cryptrack.features.shared.services.AutocompleteDatabasePopulatorReciever
import com.sidbola.cryptrack.features.shared.services.AutocompleteDatabasePopulatorService
import kotlinx.android.synthetic.main.fragment_coin_search.*

const val INITIAL_WATCHLIST_LOADED = "watchlistLoadedFirstTime"

class CoinSearchFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    lateinit var coinNameList: MutableList<String>
    lateinit var autcompleteDataReceiver: AutocompleteDatabasePopulatorReciever
    lateinit var db: DatabaseHandler
    lateinit var coinListAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userPreferences = activity?.getSharedPreferences(USER_PREFERENCES_FILENAME, Context.MODE_PRIVATE)

        if (userPreferences?.getBoolean(INITIAL_WATCHLIST_LOADED, false) == false) {
            coinSearch.alpha = 0f

            setupServiceReceiver()
            onStartService()
        } else {
            fetchingCoinsIndicator.visibility = View.GONE
            fetchingProgress.visibility = View.GONE

            db = context?.let { DatabaseHandler(it) }!!
            coinNameList = db.getAllCoinEntries()

            coinListAdapter = ArrayAdapter(activity,
                android.R.layout.simple_dropdown_item_1line,
                coinNameList)
            coinSearch.setAdapter(coinListAdapter)
        }

        coinSearch.setOnItemClickListener { _, _, _, _ ->
            val selectedItem = coinSearch.text.toString()
            val temp = selectedItem.drop(2).split("]")
            val other = temp[0].dropLast(1)

            coinSearch.setText("")

            mListener?.onCoinSearchFragmentInteraction(other, true)
            activity?.hideKeyboard(coinSearch)
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun onStartService() {
        val autocompleteData = Intent(context, AutocompleteDatabasePopulatorService::class.java)
        autocompleteData.putExtra("receiver", autcompleteDataReceiver)
        activity?.startService(autocompleteData)
    }

    private fun setupServiceReceiver() {
        autcompleteDataReceiver = AutocompleteDatabasePopulatorReciever(Handler())
        autcompleteDataReceiver.setReceiver(object : AutocompleteDatabasePopulatorReciever.Receiver {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                if (resultCode == Activity.RESULT_OK) {

                    val userPreferences = activity?.getSharedPreferences(USER_PREFERENCES_FILENAME, Context.MODE_PRIVATE)

                    val editor: SharedPreferences.Editor = userPreferences!!.edit()
                    editor.putBoolean(INITIAL_WATCHLIST_LOADED, true)
                    editor.apply()

                    fetchingProgress.animate().setDuration(1000).alpha(0.0f).setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            fetchingProgress.visibility = View.GONE
                        }
                    })

                    fetchingCoinsIndicator.animate().setDuration(1000).alpha(0.0f).setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            fetchingCoinsIndicator.visibility = View.GONE

                            coinSearch.animate().setDuration(1000).alpha(1f).setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                }
                            })
                        }
                    })

                    db = context?.let { DatabaseHandler(it) }!!
                    coinNameList = db.getAllCoinEntries()
                    coinListAdapter = ArrayAdapter(activity,
                        android.R.layout.simple_dropdown_item_1line,
                        coinNameList)
                    coinSearch.setAdapter(coinListAdapter)

                    db.changeWatchStatus("BTC")
                    mListener?.onCoinSearchFragmentInteraction("BTC", false)
                    db.changeWatchStatus("ETH")
                    mListener?.onCoinSearchFragmentInteraction("ETH", false)

                    db.changeWatchStatus("XRP")
                    mListener?.onCoinSearchFragmentInteraction("XRP", false)

                    db.changeWatchStatus("BCH")
                    mListener?.onCoinSearchFragmentInteraction("BCH", false)

                    db.changeWatchStatus("EOS")
                    mListener?.onCoinSearchFragmentInteraction("EOS", false)



                }
            }
        })
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
        fun onCoinSearchFragmentInteraction(ticker: String, isSearching: Boolean)
    }
}
