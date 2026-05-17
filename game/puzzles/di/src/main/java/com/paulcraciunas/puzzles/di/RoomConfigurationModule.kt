package com.paulcraciunas.puzzles.di

import com.paulcraciunas.puzzles.api.PuzzleDatabaseContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DbName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DbAssetPath

@Module
@InstallIn(SingletonComponent::class)
object RoomConfigurationModule {
    @Provides
    @DbName
    fun provideDbName(): String = PuzzleDatabaseContract.ROOM_DATABASE_NAME

    @Provides
    @DbAssetPath
    fun provideDbAssetPath(): String = PuzzleDatabaseContract.LITE_ASSET_PATH
}
