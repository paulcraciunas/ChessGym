package com.paulcraciunas.puzzles.di

import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.puzzles.impl.network.progress.WorkerProgressReporter
import com.paulcraciunas.puzzles.impl.network.source.LichessDatabaseSource
import com.paulcraciunas.puzzles.impl.network.source.PuzzleDatabaseSource
import com.paulcraciunas.puzzles.impl.network.unpack.FileDecompressor
import com.paulcraciunas.puzzles.impl.network.unpack.ZstdFileDecompressor
import com.paulcraciunas.puzzles.impl.network.writer.FileProgressWriter
import com.paulcraciunas.puzzles.impl.network.writer.FileWriter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WorkerModule {
    @Binds
    @Singleton
    abstract fun databaseSource(impl: LichessDatabaseSource): PuzzleDatabaseSource

    @Binds
    @Singleton
    abstract fun progressReporter(impl: WorkerProgressReporter): ProgressReporter

    @Binds
    @Singleton
    abstract fun fileDecompressor(impl: ZstdFileDecompressor): FileDecompressor

    @Binds
    @Singleton
    abstract fun fileWriter(impl: FileProgressWriter): FileWriter
}
