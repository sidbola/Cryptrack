package com.sidbola.cryptrack.features.shared.services

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class AutocompleteDatabasePopulatorReciever(handler: Handler) : ResultReceiver(handler) {

    private lateinit var receiver: Receiver

    fun setReceiver(receiver: Receiver) {
        this.receiver = receiver
    }

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        if (::receiver.isInitialized && resultData != null) {
            receiver.onReceiveResult(resultCode, resultData)
        }
    }
}