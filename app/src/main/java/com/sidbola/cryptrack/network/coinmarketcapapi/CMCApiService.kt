// package com.sidbola.cryptrack.api_calls.CoinMarketCapApi
//
//
// import com.sidbola.cryptrack.features.shared.model.CMCCoinData
// import retrofit2.Retrofit
// import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
// import retrofit2.converter.gson.GsonConverterFactory
// import retrofit2.http.GET
// import retrofit2.http.Query
//
//
// interface CMCApiService {
//
// @GET("v1/ticker")
// fun getCoinData(@Query("start") start: Int,
// @Query("limit") limit: Int): io.reactivex.Observable<List<CMCCoinData>>
//
// companion object Factory{
// fun create(): CMCApiService {
// val retrofit = Retrofit.Builder()
// .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
// .addConverterFactory(GsonConverterFactory.create())
// .baseUrl("https://api.coinmarketcap.com/")
// .build()
//
// return retrofit.create(CMCApiService::class.java)
// }
// }
// }