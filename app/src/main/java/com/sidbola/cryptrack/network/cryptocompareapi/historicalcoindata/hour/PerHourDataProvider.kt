package com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.hour

object PerHourDataProvider {

    fun provideHistoricalDataPerHour(): GetHistoricalDataPerHour {
        return GetHistoricalDataPerHour(PerHourApiService.create())
    }
}