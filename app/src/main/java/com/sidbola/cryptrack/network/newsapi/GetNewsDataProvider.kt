package com.sidbola.cryptrack.network.newsapi

object GetNewsDataProvider {

    fun provideGetCoinData(): GetNewsData {
        return GetNewsData(NewsApiService.create())
    }
}