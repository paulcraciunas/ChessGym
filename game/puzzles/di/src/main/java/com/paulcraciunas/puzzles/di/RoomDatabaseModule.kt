package com.paulcraciunas.puzzles.di

import android.content.Context
import androidx.room.Room
import com.paulcraciunas.puzzles.impl.impl.AbstractPuzzleDatabase
import com.paulcraciunas.puzzles.impl.impl.PuzzleDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RoomDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AbstractPuzzleDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AbstractPuzzleDatabase::class.java,
            DB_NAME
        ).fallbackToDestructiveMigration(true)
            .build()

    private const val DB_NAME = "puzzle_database"
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DatabaseModule {
    @Binds
    abstract fun puzzleDatabase(impl: AbstractPuzzleDatabase): PuzzleDatabase
}
