package com.paulcraciunas.global.sounds

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.paulcraciunas.global.resources.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameSoundManager(
    dispatcher: CoroutineDispatcher,
    context: Context,
) : SoundManager {
    private val audioScope = CoroutineScope(dispatcher)
    private var tickJob: Job? = null
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val moveSound = soundPool.load(context, R.raw.move, PRIORITY_DEFAULT)
    private val gameStartSound = soundPool.load(context, R.raw.game_start, PRIORITY_DEFAULT)
    private val gameOverSound = soundPool.load(context, R.raw.game_over, PRIORITY_DEFAULT)
    private val tickSound = soundPool.load(context, R.raw.tick, PRIORITY_DEFAULT)

    override fun playMove() {
        soundPool.play(moveSound, FULL_VOLUME, FULL_VOLUME, PRIORITY_DEFAULT, NO_LOOP, NORMAL_RATE)
    }

    override fun playGameStart() {
        soundPool.play(gameStartSound, FULL_VOLUME, FULL_VOLUME, PRIORITY_DEFAULT, NO_LOOP, NORMAL_RATE)
    }

    override fun playGameOver() { // higher priority, since it's game over
        soundPool.play(gameOverSound, FULL_VOLUME, FULL_VOLUME, PRIORITY_HIGH, NO_LOOP, NORMAL_RATE)
        stopTick()
    }

    override fun startTickCountdown(totalSeconds: Int) {
        if (tickJob?.isActive == true) return

        tickJob = audioScope.launch {
            for (second in 1..totalSeconds) {
                // Calculate volume curve: start at 50%, ramp up linearly to 100%
                val progress = second.toFloat() / totalSeconds.toFloat()
                val dynamicVolume = 0.5f + (progress * 0.5f)

                soundPool.play(tickSound, dynamicVolume, dynamicVolume, PRIORITY_DEFAULT, NO_LOOP, NORMAL_RATE)
                delay(500L)
                soundPool.play(tickSound, dynamicVolume, dynamicVolume, PRIORITY_DEFAULT, NO_LOOP, NORMAL_RATE)
                delay(500L)
            }
        }
    }

    override fun release() {
        audioScope.cancel()
        soundPool.release()
    }

    private fun stopTick() {
        tickJob?.cancel()
        tickJob = null
    }

    companion object {
        private const val MAX_STREAMS = 5 // so we can play moves, ticks and game-over simultaneously
        private const val FULL_VOLUME = 1.0f
        private const val PRIORITY_DEFAULT = 1
        private const val PRIORITY_HIGH = 2
        private const val NO_LOOP = 0
        private const val NORMAL_RATE = 1.0f
    }
}
