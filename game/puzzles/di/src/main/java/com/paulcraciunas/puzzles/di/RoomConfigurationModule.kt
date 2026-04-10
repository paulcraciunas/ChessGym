package com.paulcraciunas.puzzles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

enum class DbVariant {
    FileSystem,
    Asset
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DbName

@Module
@InstallIn(SingletonComponent::class)
object RoomConfigurationModule {
    @Provides
    @DbName
    fun provideDbName(): String = "puzzle_database"

    @Provides
    fun provideDbVariant(): DbVariant = DbVariant.FileSystem
}
