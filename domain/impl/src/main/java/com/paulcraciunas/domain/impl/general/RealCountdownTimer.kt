package com.paulcraciunas.domain.impl.general

import com.paulcraciunas.domain.api.general.CountdownTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Duration
import javax.inject.Inject

class RealCountdownTimer @Inject constructor(
    private val clock: Clock = Clock.systemUTC(),
) : CountdownTimer {

    override fun start(durationMs: Long, intervalMillis: Long): Flow<CountdownTimer.Remainder> = flow {
        val interval = intervalMillis.coerceIn(1L, 10_000L)
        val startTime = clock.instant()
        // Anchor the deadline to absolute wall-clock time to prevent delay-drift
        val deadline = startTime.plusMillis(durationMs)

        var now = clock.instant()
        while (now.isBefore(deadline)) {
            val totalRemainingMs = Duration.between(now, deadline).toMillis()
            emit(
                CountdownTimer.Remainder(
                    seconds = (totalRemainingMs / 1000).toInt(),
                    millis = (totalRemainingMs % 1000).toInt()
                )
            )

            // Dynamic correction: Sleep for the standard interval, but
            // even if delay() wakes up late, the next loop recalculates against the absolute deadline
            delay(interval)
            now = clock.instant()
        }
        emit(CountdownTimer.Remainder(seconds = 0, millis = 0)) // Final deterministic emission
    }
}
