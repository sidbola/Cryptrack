package com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.day

import com.sidbola.cryptrack.features.shared.model.HistoricalDataResponse

class GetHistoricalDataPerDay(private val apiService: PerDayApiService) {

    fun getHistoricalDataPerDay(fsym: String, tsym: String, limit: Int, allData: String = "false"): io.reactivex.Observable<HistoricalDataResponse> {
        return apiService.getHistoricalDataPerDay(fsym, tsym, limit, allData)
    }
}