package com.sidbola.cryptrack.network.newsapi

import com.sidbola.cryptrack.features.shared.model.NewsCallResponse

class GetNewsData(private val apiService: NewsApiService) {
    fun getNewsData(query: String, sources: String, apiKey: String): io.reactivex.Observable<NewsCallResponse> {
        return apiService.getNewsData(query, sources, apiKey)
    }
}