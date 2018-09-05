package com.sidbola.cryptrack.network.cryptocompareapi.aggregatecoindata

object GetCCCoinDataProvider {

    fun provideGetCoinData(): GetCCCoinData {
        return GetCCCoinData(CCApiService.create())
    }
}