package com.sidbola.cryptrack.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

// Define database name
const val DATABASE_NAME = "CryptrackDB"

// Define table and column names for watchlist table
const val WATCHLIST_TABLE_NAME = "Watchlist"
const val COL_TICKER = "ticker"
const val COL_NAME = "name"
const val COL_LIST_NAME = "listName"
const val COL_IS_WATCHED = "isWatched"

// Define table and column names for transactions table
const val TRANSACTIONS_TABLE_NAME = "Transactions"
const val COL_TRANSACTION_ID = "transactionId"
const val COL_BUY_PRICE = "buyPrice"
const val COL_AMOUNT = "amount"
const val COL_DATE = "date"
const val COL_TYPE = "type"

class DatabaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        // SQL statement to create watchlist table
        val createWatchlistTable = "CREATE TABLE " + WATCHLIST_TABLE_NAME + " (" +
                COL_TICKER + " VARCHAR(256) PRIMARY KEY," +
                COL_NAME + " VARCHAR(256)," +
                COL_LIST_NAME + " VARCHAR(256)," +
                COL_IS_WATCHED + " INTEGER);"

        // SQL statement to create transactions table
        val createTransactionsTable = "CREATE TABLE " + TRANSACTIONS_TABLE_NAME + " (" +
                COL_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                COL_TICKER + " VARCHAR(256)," +
                COL_BUY_PRICE + " FLOAT," +
                COL_AMOUNT + " FLOAT," +
                COL_DATE + " VARCHAR(256)," +
                COL_TYPE + " VARCHAR(256));"

        // Execute sql statements defined above
        db?.execSQL(createWatchlistTable)
        db?.execSQL(createTransactionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    /**
     * Function to get all coin entries from watchlist table
     *
     * @return list of all coins in watchlist table as strings
     */
    fun getAllCoinEntries(): MutableList<String> {
        val list: MutableList<String> = ArrayList()

        val db = this.readableDatabase
        val query = "SELECT * from $WATCHLIST_TABLE_NAME"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                list.add(result.getString(result.getColumnIndex(COL_LIST_NAME)))
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    fun getAllWatchedCoinTickers(): MutableList<String> {
        val list: MutableList<String> = ArrayList()

        val db = this.readableDatabase
        val query = "SELECT * from " + WATCHLIST_TABLE_NAME +
                " WHERE isWatched = 1;"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                list.add(result.getString(result.getColumnIndex(COL_TICKER)))
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    fun getWatchStatus(ticker: String): Int {
        val db = this.readableDatabase
        val query = "SELECT *" + " FROM " + WATCHLIST_TABLE_NAME +
                " WHERE ticker = '" + ticker + "';"
        val result = db.rawQuery(query, null)

        var status = 0

        if (result.moveToFirst()) {
            status = result.getInt(result.getColumnIndex(COL_IS_WATCHED))
        }

        result.close()
        db.close()
        return status
    }

    fun changeWatchStatus(ticker: String) {
        val db = this.writableDatabase
        val query = "SELECT *" + " FROM " + WATCHLIST_TABLE_NAME +
                " WHERE ticker = '" + ticker + "';"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            if (result.getInt(result.getColumnIndex(COL_IS_WATCHED)) == 0) {
                val setTrue = "UPDATE " + WATCHLIST_TABLE_NAME +
                        " SET isWatched = 1 " +
                        "WHERE ticker = '" + ticker + "';"
                db?.execSQL(setTrue)
                val resultString = "Added " + result.getString(result.getColumnIndex(COL_NAME)) + " to watchlist"
                Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show()
            } else {
                val setFalse = "UPDATE " + WATCHLIST_TABLE_NAME +
                        " SET " + COL_IS_WATCHED + " = 0 " +
                        "WHERE ticker = '" + ticker + "';"
                db?.execSQL(setFalse)
                val resultString = "Removed " + result.getString(result.getColumnIndex(COL_NAME)) + " from watchlist"
                Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show()
            }
        }

        result.close()
        db.close()
    }

    fun insertWatchlistData(coinList: ArrayList<WatchlistCoin>) {
        val db = this.writableDatabase

        db.beginTransaction()

        for (coin in coinList) {
            val cv = ContentValues()
            cv.put(COL_TICKER, coin.ticker)
            cv.put(COL_NAME, coin.name)
            cv.put(COL_LIST_NAME, coin.listName)
            cv.put(COL_IS_WATCHED, coin.isWatched)
            db.insertWithOnConflict(WATCHLIST_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        }

        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
    }

    fun insertTransaction(transaction: Transaction) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_TICKER, transaction.ticker)
        cv.put(COL_BUY_PRICE, transaction.buyPrice)
        cv.put(COL_AMOUNT, transaction.amount)
        cv.put(COL_DATE, transaction.date)
        cv.put(COL_TYPE, transaction.type)

        val result = db.insert(TRANSACTIONS_TABLE_NAME, null, cv)

        if (result == (-1).toLong()) {
            Toast.makeText(context, "Store Failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAllTransactions(): MutableList<Transaction> {
        val list: MutableList<Transaction> = ArrayList()

        val db = this.readableDatabase
        val query = "SELECT * from $TRANSACTIONS_TABLE_NAME"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val ticker = result.getString(result.getColumnIndex(COL_TICKER))
                val price = result.getString(result.getColumnIndex(COL_BUY_PRICE))
                val amount = result.getString(result.getColumnIndex(COL_AMOUNT))
                val date = result.getString(result.getColumnIndex(COL_DATE))
                val type = result.getString(result.getColumnIndex(COL_TYPE))
                val id = result.getInt(result.getColumnIndex(COL_TRANSACTION_ID))

                list.add(Transaction(ticker, price.toDouble(), amount.toDouble(), date, type, id))
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    fun deleteTransaction(id: Int): Boolean {
        val db = this.writableDatabase

        return db.delete(TRANSACTIONS_TABLE_NAME, "$COL_TRANSACTION_ID='$id'", null) > 0
    }

    fun getTransactionsForTicker(ticker: String): MutableList<Transaction> {
        val list: MutableList<Transaction> = ArrayList()

        val db = this.readableDatabase
        val query = "SELECT *" + " FROM " + TRANSACTIONS_TABLE_NAME +
                " WHERE ticker = '" + ticker + "';"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val coinTicker = result.getString(result.getColumnIndex(COL_TICKER))
                val price = result.getString(result.getColumnIndex(COL_BUY_PRICE))
                val amount = result.getString(result.getColumnIndex(COL_AMOUNT))
                val date = result.getString(result.getColumnIndex(COL_DATE))
                val type = result.getString(result.getColumnIndex(COL_TYPE))
                val id = result.getInt(result.getColumnIndex(COL_TRANSACTION_ID))

                list.add(Transaction(coinTicker, price.toDouble(), amount.toDouble(), date, type, id))
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }
}
