package com.sidbola.cryptrack.network.newsapi

import com.sidbola.cryptrack.features.shared.model.NewsCallResponse
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/everything")
    fun getNewsData(
        @Query("q") query: String,
        @Query("sources") sources: String,
        @Query("apiKey") key: String
    ): io.reactivex.Observable<NewsCallResponse>

    companion object Factory {
        fun create(): NewsApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://newsapi.org/")
                    .build()

            return retrofit.create(NewsApiService::class.java)
        }
    }
}