package com.paulcraciunas.chessgym.startup

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.global.qualifiers.MainDispatcher
import com.paulcraciunas.global.resources.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class ResourcePreWarmer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:MainDispatcher private val main: CoroutineDispatcher,
    @param:IoDispatcher private val io: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(main + SupervisorJob())
    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    init {
        scope.launch {
            try {
                coroutineScope {
                    withContext(io) { // Offload heavy font parsing to the I/O thread
                        fonts.forEach { fontRes ->
                            runCatching {
                                ResourcesCompat.getFont(context, fontRes)
                            }.onFailure { Timber.w(it, "Failed to pre-warm font: $fontRes") }
                        }
                    }
                    withContext(main) {
                        drawables.forEach { drawableRes -> // Process lightweight drawables sequentially on the Main thread
                            runCatching {
                                ResourcesCompat.getDrawable(context.resources, drawableRes, context.theme)
                            }.onFailure { Timber.w(it, "Failed to pre-warm drawable: $drawableRes") }
                        }
                    }
                }
            } catch (coOp: CancellationException) {
                throw coOp
            } catch (e: Exception) {
                Timber.w(e, "Resource pre-warming failed or was cancelled")
            } finally {
                _isComplete.value = true
            }
        }
    }

    companion object {
        private val fonts = intArrayOf(
            R.font.inter_tight_regular,
            R.font.inter_tight_medium,
            R.font.inter_tight_semibold,
            R.font.inter_tight_bold,
            R.font.instrument_serif_regular,
            R.font.jetbrains_mono_medium,
            R.font.jetbrains_mono_semibold,
        )

        private val drawables = com.paulcraciunas.global.resources.PreWarmDrawables.list
    }
}
