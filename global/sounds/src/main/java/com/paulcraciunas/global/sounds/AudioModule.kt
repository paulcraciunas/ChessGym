package com.paulcraciunas.global.sounds

import android.content.Context
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    @Provides
    @Singleton
    fun provideSoundManager(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
        @ApplicationContext context: Context,
    ): SoundManager = GameSoundManager(dispatcher, context)
}
