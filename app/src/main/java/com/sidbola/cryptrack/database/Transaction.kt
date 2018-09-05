package com.sidbola.cryptrack.database

class Transaction(
    var ticker: String,
    var buyPrice: Double,
    var amount: Double,
    var date: String,
    var type: String,
    var transactionId: Int
)