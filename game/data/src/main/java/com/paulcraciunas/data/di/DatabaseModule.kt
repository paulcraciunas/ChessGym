package com.paulcraciunas.data.di

import android.content.Context
import androidx.room.Room
import com.paulcraciunas.data.api.PuzzleDatabase
import com.paulcraciunas.data.api.PuzzleRepository
import com.paulcraciunas.data.api.PuzzleRepositoryImpl
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

    @Provides
    @Singleton
    fun provideRepository(db: PuzzleDatabase): PuzzleRepository = PuzzleRepositoryImpl(db)

    private const val DB_NAME = "puzzle_database"
}
