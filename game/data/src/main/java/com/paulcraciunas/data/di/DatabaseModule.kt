package com.paulcraciunas.data.di

import android.content.Context
import androidx.room.Room
import com.paulcraciunas.data.impl.PuzzleAdapter
import com.paulcraciunas.data.impl.PuzzleDatabase
import com.paulcraciunas.data.impl.PuzzleRepositoryImpl
import com.paulcraciunas.domain.PuzzleRepository
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
    fun provideRepository(db: PuzzleDatabase, adapter: PuzzleAdapter): PuzzleRepository = PuzzleRepositoryImpl(db, adapter)

    private const val DB_NAME = "puzzle_database"
}
