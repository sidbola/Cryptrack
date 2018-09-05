package com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.minute

/**
 * Created by Satjit on 1/8/18.
 */
object PerMinuteDataProvider {

    fun provideHistoricalDataPerMinute(): GetHistoricalDataPerMinute {
        return GetHistoricalDataPerMinute(PerMinuteApiService.create())
    }
}