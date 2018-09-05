package com.sidbola.cryptrack.features.shared.services

import android.app.Activity
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.sidbola.cryptrack.database.DatabaseHandler
import com.sidbola.cryptrack.database.WatchlistCoin
import com.sidbola.cryptrack.features.shared.model.CCCoinData
import com.sidbola.cryptrack.network.cryptocompareapi.aggregatecoindata.GetCCCoinDataProvider
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

class AutocompleteDatabasePopulatorService : IntentService("AutocompleteService") {

    lateinit var context: Context
    private lateinit var db: DatabaseHandler
    private lateinit var rec: ResultReceiver

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    override fun onHandleIntent(p0: Intent?) {

        rec = p0!!.getParcelableExtra("receiver")

        db = context.let { DatabaseHandler(it) }

        val coinUrls = GetCCCoinDataProvider.provideGetCoinData()
        coinUrls.getCCoinData()
            .subscribe { result ->
                runErrands(result.Data)

//                val bundle = Bundle()
//
//                for (coin in result.Data!!){
//                    val watchlistEntry = WatchlistCoin(coin.value.Name.toString(), coin.value.CoinName.toString(), 0)
//                    db.insertWatchlistData(watchlistEntry)
//                }
//
//                bundle.putString("resultValue", "Finished loading database")
//                rec.send(Activity.RESULT_OK, bundle)
            }
    }

    private fun runErrands(coinData: Map<String, CCCoinData>?) = async {

        val bundle = Bundle()

        val coinList = ArrayList<WatchlistCoin>()

        coinData!!.map { coin ->
            coinList.add(WatchlistCoin(coin.value.Name.toString(), coin.value.CoinName.toString(), 0))
        }

        try {
            val job = async(CommonPool) {
                db.insertWatchlistData(coinList)
            }

            job.await()
            bundle.putString("resultValue", "Finished loading database")
            rec.send(Activity.RESULT_OK, bundle)
        } catch (e: Exception) {
        }
    }
}
