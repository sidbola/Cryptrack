package com.sidbola.cryptrack.network.cryptocompareapi.aggregatecoindata

import com.sidbola.cryptrack.features.shared.model.CCResponse

class GetCCCoinData(private val apiService: CCApiService) {

    fun getCCoinData(): io.reactivex.Observable<CCResponse> {
        return apiService.getCoinData()
    }
}