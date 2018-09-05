package com.sidbola.cryptrack.network.cryptocompareapi.aggregatecoindata

import com.sidbola.cryptrack.features.shared.model.CCResponse
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface CCApiService {

    @GET("data/all/coinlist")
    fun getCoinData(): io.reactivex.Observable<CCResponse>

    companion object Factory {
        fun create(): CCApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://min-api.cryptocompare.com/")
                    .build()

            return retrofit.create(CCApiService::class.java)
        }
    }
}