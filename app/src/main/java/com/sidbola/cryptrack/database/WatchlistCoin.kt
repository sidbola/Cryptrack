package com.sidbola.cryptrack.database

class WatchlistCoin(var ticker: String, var name: String, var isWatched: Int) {

    var listName: String = ""

    init {
        this.listName = "[ $ticker ]  $name"
    }
}