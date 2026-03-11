package com.paulcraciunas.chessgym.di

import com.paulcraciunas.puzzles.di.DbName
import com.paulcraciunas.puzzles.di.DbVariant
import com.paulcraciunas.puzzles.di.RoomConfigurationModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RoomConfigurationModule::class]
)
object TestDatabaseModule {
    @Provides
    @DbName
    fun provideDbName(): String = "test_puzzles.db"

    @Provides
    fun provideDbVariant(): DbVariant = DbVariant.Asset
}
