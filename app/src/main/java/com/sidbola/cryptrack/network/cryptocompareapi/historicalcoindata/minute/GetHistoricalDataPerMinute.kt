package com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.minute

import com.sidbola.cryptrack.features.shared.model.HistoricalDataResponse

class GetHistoricalDataPerMinute(private val apiService: PerMinuteApiService) {

    fun getHistoricalDataPerMinute(fsym: String, tsym: String, limit: Int): io.reactivex.Observable<HistoricalDataResponse> {
        return apiService.getHistoricalDataPerMinute(fsym, tsym, limit)
    }
}