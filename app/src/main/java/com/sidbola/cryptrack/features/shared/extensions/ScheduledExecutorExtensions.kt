package com.sidbola.cryptrack.features.shared.extensions

import java.util.concurrent.Executors.callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

fun <V, T : ScheduledExecutorService> T.schedule(
    delay: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    action: () -> V
): ScheduledFuture<Any>? {
    return this.schedule(
            callable { action() },
            delay, unit)
}