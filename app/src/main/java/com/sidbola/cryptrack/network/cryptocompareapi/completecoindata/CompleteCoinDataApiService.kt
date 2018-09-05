// package com.sidbola.cryptrack.api_calls.CryptoCompareApi.HistoricalCoinData.by_day
//
// import com.sidbola.cryptrack.features.shared.model.FullPriceInfoResponse
// import retrofit2.Retrofit
// import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
// import retrofit2.converter.gson.GsonConverterFactory
// import retrofit2.http.GET
// import retrofit2.http.Query
//
// interface CompleteCoinDataApiService {
//    @GET("data/pricemultifull")
//    fun getCompleteDataPerCoin(@Query("fsyms") fsym: String,
//                                 @Query("tsyms") tsym: String): io.reactivex.Observable<FullPriceInfoResponse>
//
//    companion object CompleteDataPerCoinFactory {
//        fun create(): CompleteCoinDataApiService {
//            val retrofit = Retrofit.Builder()
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .baseUrl("https://min-api.cryptocompare.com/")
//                    .build()
//
//            return retrofit.create(CompleteCoinDataApiService::class.java)
//        }
//    }
// }