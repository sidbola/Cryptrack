package com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.day

object PerDayDataProvider {

    fun provideHistoricalDataPerDay(): GetHistoricalDataPerDay {
        return GetHistoricalDataPerDay(PerDayApiService.create())
    }
}