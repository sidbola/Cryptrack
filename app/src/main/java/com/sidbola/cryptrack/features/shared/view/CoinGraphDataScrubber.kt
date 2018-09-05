package com.sidbola.cryptrack.features.shared.view

import com.sidbola.cryptrack.features.shared.model.CoinDataSnapshot
import com.sidbola.cryptrack.features.shared.model.ScrubbedCoinData

class CoinGraphDataScrubber {
    fun scrubData(dataSet: ArrayList<CoinDataSnapshot>): ScrubbedCoinData {
        var min = Float.MAX_VALUE
        val pricePoints = ArrayList<Float>()
        val dataPoints = ArrayList<FloatPoint>()
        val dollarChange = dataSet[dataSet.size - 1].close.toFloat() - dataSet[0].close.toFloat()
        val percentChange = (dollarChange / dataSet[0].close.toFloat() * 100)
        var changePositive = false
        var percentChangeString: String

        percentChangeString = percentChange.toString()
        val before = percentChangeString.substringBefore(".")
        val after = percentChangeString.substringAfter(".")

        percentChangeString = when {
            after.length > 1 -> before + "." + after.substring(0, 2)
            after.length == 1 -> before + "." + after.substring(0, 1)
            else -> before
        }

        if (percentChange > 0) {
            percentChangeString = "+$percentChangeString% ↑"
            changePositive = true
        } else {
            percentChangeString = "$percentChangeString% ↓"
        }

        dataSet.asSequence()
                .filter { it.close.toFloat() < min }
                .forEach { min = it.close.toFloat() }

        var xPosition = 0f

        for (coin in dataSet) {
            dataPoints.add(FloatPoint(xPosition, coin.close.toFloat() - min))
            xPosition++
            pricePoints.add(coin.close.toFloat())
        }

        var tempMax = -9999999999999f
        dataPoints
                .asSequence()
                .filter { it.y > tempMax }
                .forEach { tempMax = it.y }

        return ScrubbedCoinData(dataPoints, pricePoints, tempMax, percentChange, percentChangeString, dollarChange, changePositive)
    }
}