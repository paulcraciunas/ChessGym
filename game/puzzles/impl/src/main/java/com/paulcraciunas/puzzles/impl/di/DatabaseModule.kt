package com.paulcraciunas.puzzles.impl.di

import android.content.Context
import androidx.room.Room
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.puzzles.impl.impl.PuzzleDatabase
import com.paulcraciunas.puzzles.impl.impl.PuzzleRepositoryImpl
import com.paulcraciunas.puzzles.impl.impl.RandomFactory
import com.paulcraciunas.puzzles.impl.impl.TLRandomFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PuzzleDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            PuzzleDatabase::class.java,
            DB_NAME
        ).fallbackToDestructiveMigration(true)
            .build()

    private const val DB_NAME = "puzzle_database"
}
