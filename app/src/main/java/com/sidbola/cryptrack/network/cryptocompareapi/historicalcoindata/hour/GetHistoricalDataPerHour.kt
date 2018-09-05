package com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.hour

import com.sidbola.cryptrack.features.shared.model.HistoricalDataResponse

class GetHistoricalDataPerHour(private val apiService: PerHourApiService) {

    fun getHistoricalDataPerHour(fsym: String, tsym: String, limit: Int): io.reactivex.Observable<HistoricalDataResponse> {
        return apiService.getHistoricalDataPerHour(fsym, tsym, limit)
    }
}