package com.paulcraciunas.puzzles.di

//noinspection PureDomain
import android.content.Context
//noinspection PureDomain
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
    fun provideDatabase(
        @ApplicationContext context: Context,
        @DbName dbName: String,
        variant: DbVariant,
    ): AbstractPuzzleDatabase {
        val builder = Room.databaseBuilder(
            context.applicationContext,
            AbstractPuzzleDatabase::class.java,
            dbName
        )
        if (variant == DbVariant.Asset) builder.createFromAsset(dbName)

        return builder.fallbackToDestructiveMigration(true)
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DatabaseModule {
    @Binds
    abstract fun puzzleDatabase(impl: AbstractPuzzleDatabase): PuzzleDatabase
}
