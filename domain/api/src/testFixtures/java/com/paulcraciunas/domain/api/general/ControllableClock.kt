package com.paulcraciunas.domain.api.general

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicLong

/**
 * A controllable [Clock] for testing that allows advancing time manually.
 * Virtual time starts at a fixed epoch and advances only when [advanceBy] is called.
 */
class ControllableClock(private val baseInstant: Instant = Instant.parse("2026-01-01T00:00:00Z")) : Clock() {
    private val offsetMillis = AtomicLong(0)

    fun advanceBy(millis: Long) {
        offsetMillis.addAndGet(millis)
    }

    override fun instant(): Instant = baseInstant.plusMillis(offsetMillis.get())
    override fun withZone(zone: ZoneId?): Clock = this
    override fun getZone(): ZoneId = ZoneId.of("UTC")
}
