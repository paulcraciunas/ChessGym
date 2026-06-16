package com.paulcraciunas.domain.api.general

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulerBackedTestClock(
    private val scheduler: TestCoroutineScheduler,
    private val baseInstant: Instant = Instant.parse("2026-01-01T00:00:00Z")
) : Clock() {

    override fun instant(): Instant = baseInstant.plusMillis(scheduler.currentTime)
    override fun getZone(): ZoneId = ZoneId.systemDefault()
    override fun withZone(zone: ZoneId?): Clock = this
}
