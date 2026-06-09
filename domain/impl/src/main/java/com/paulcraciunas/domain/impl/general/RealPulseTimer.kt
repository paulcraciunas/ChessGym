package com.paulcraciunas.domain.impl.general

import com.paulcraciunas.domain.api.general.PulseTimer
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.Clock
import java.time.Duration
import javax.inject.Inject

class RealPulseTimer @Inject constructor(
    private val clock: Clock,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : PulseTimer {

    override fun start(intervalMillis: Long): Flow<Long> = flow {
        var lastTick = clock.instant()

        while (true) {
            delay(intervalMillis)
            val now = clock.instant()
            val elapsedThisTick = Duration.between(lastTick, now).toMillis()
            lastTick = now

            emit(elapsedThisTick)
        }
    }.flowOn(dispatcher)
}
