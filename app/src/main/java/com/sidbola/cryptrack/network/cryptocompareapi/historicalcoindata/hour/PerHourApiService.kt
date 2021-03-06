package com.sidbola.cryptrack.network.cryptocompareapi.historicalcoindata.hour

import com.sidbola.cryptrack.features.shared.model.HistoricalDataResponse
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PerHourApiService {
    @GET("data/histohour")
    fun getHistoricalDataPerHour(
        @Query("fsym") fsym: String,
        @Query("tsym") tsym: String,
        @Query("limit") limit: Int
    ): io.reactivex.Observable<HistoricalDataResponse>

    companion object HistoricalDataPerHourFactory {
        fun create(): PerHourApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://min-api.cryptocompare.com/")
                    .build()

            return retrofit.create(PerHourApiService::class.java)
        }
    }
}