package com.sidbola.cryptrack.features.shared.model

import com.sidbola.cryptrack.database.Transaction
import com.sidbola.cryptrack.features.shared.view.FloatPoint

data class CoinData(
    var high24hr: String = "",
    var low24hr: String = "",
    var marketCapDisplay: String = "",
    var marketCapRaw: Float = 0f,
    var name: String = "",
    var open24hr: String = "",
    var percentChange: String = "",
    var price: String = "",
    var supply: String = "",
    var ticker: String = "",
    var volume24hr: String = "",
    var perHourData: ArrayList<CoinDataSnapshot>? = ArrayList()
)

data class DiscoverData(
    var top100: MutableList<CoinData> = mutableListOf(),
    var topWinners: MutableList<CoinData> = mutableListOf(),
    var topLosers: MutableList<CoinData> = mutableListOf()
)

data class ScrubbedCoinData(
    val dataPoints: ArrayList<FloatPoint>,
    val pricePoints: ArrayList<Float>,
    val max: Float,
    val percentChange: Float,
    val percentChangeString: String,
    val dollarChange: Float,
    val changePositive: Boolean
)

data class CCCoinData(
    val Id: String?,
    val Url: String?,
    val ImageUrl: String?,
    val Name: String?,
    val Symbol: String?,
    val CoinName: String?,
    val FullName: String?,
    val Algorithm: String?,
    val ProofType: String?,
    val FullyPremined: String?,
    val TotalCoinSupply: String?,
    val PreMinedValue: String?,
    val TotalCoinsFreeFloat: String?,
    val SortOrder: String?,
    val Sponsored: Boolean?
)

data class CCResponse(
    val Response: String?,
    val Message: String?,
    val BaseImageUrl: String?,
    val BaseLinkUrl: String?,
    val DefaultWatchlist: DefaultWatchlist?,
    val Data: Map<String, CCCoinData>?
)

data class DefaultWatchlist(
    val Coinls: String?,
    val Sponsored: String?
)

data class HistoricalDataResponse(
    val Response: String?,
    val Type: Int?,
    val Aggregated: Boolean?,
    val Data: ArrayList<CoinDataSnapshot>?
)

data class CoinDataSnapshot(
    val time: String,
    var close: String,
    val high: String,
    val low: String,
    val open: String,
    val volumefrom: String,
    val volumeto: String
)

// data class RawData(
//        var TYPE: String,
//        var MARKET: String,
//        var TOSYMBOL: String,
//        var FLAGS: String,
//        var PRICE: Float,
//        var LASTUPDATE: Float,
//        var LASTVOLUME: Float,
//        var LASTVOLUMETO: Float,
//        var LASTTRADEID: String,
//        var VOLUMEDAY: Float,
//        var VOLUMEDAYTO: Float,
//        var VOLUME24HOUR: Float,
//        var VOLUME24HOURTO: Float,
//        var OPENDAY: Float,
//        var HIGHDAY: Float,
//        var LOWDAY: Float,
//        var OPEN24HOUR: Any,
//        var HIGH24HOUR: Any,
//        var LOW24HOUR: Any,
//        var LASTMARKET: String,
//        var CHANGE24HOUR: Float,
//        var CHANGEPCT24HOUR: Float,
//        var CHANGEDAY: Float,
//        var CHANGEPCTDAY: Float,
//        var SUPPLY: Float,
//        var MKTCAP: Float,
//        var TOTALVOLUME24H: Float,
//        var TOTALVOLUME24HTO: Float
// )

// data class DisplayData(
//        var FROMSYMBOL: String,
//        var TOSYMBOL: String,
//        var MARKET: String,
//        var PRICE: String,
//        var LASTUPDATE: String,
//        var LASTVOLUME: String,
//        var LASTVOLUMETO: String,
//        var LASTTRADEID: String,
//        var VOLUMEDAY: String,
//        var VOLUMEDAYTO: String,
//        var VOLUME24HOUR: String,
//        var VOLUME24HOURTO: String,
//        var OPENDAY: String,
//        var HIGHDAY: String,
//        var LOWDAY: String,
//        var OPEN24HOUR: String,
//        var HIGH24HOUR: String,
//        var LOW24HOUR: String,
//        var LASTMARKET: String,
//        var CHANGE24HOUR: String,
//        var CHANGEPCT24HOUR: String,
//        var CHANGEDAY: String,
//        var CHANGEPCTDAY: String,
//        var SUPPLY: String,
//        var MKTCAP: String,
//        var TOTALVOLUME24H: String,
//        var TOTALVOLUME24HTO: String
// )
//
// data class CMCCoinData(
//        val id: String,
//        val name: String,
//        val symbol: String,
//        val rank: String,
//        val price_usd: String,
//        val price_btc: String,
//        val _24h_volume_usd: String,
//        val market_cap_usd: String,
//        val available_supply: String,
//        val total_supply: String,
//        val max_supply: String,
//        val percent_change_1h: String,
//        val percent_change_24h: String,
//        val percent_change_7d: String,
//        val last_updated: String
// )

data class NewsCallResponse(
    val status: String,
    val totalResults: Int,
    val articles: ArrayList<Article>
)

data class Article(
    val source: Source,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String
)

data class Source(
    val id: String,
    val name: String
)

data class TransactionAdapterItem(
    var listOfTransactions: ArrayList<Transaction> = ArrayList(),
    var ticker: String = "",
    var currentPrice: Double = 0.0
)