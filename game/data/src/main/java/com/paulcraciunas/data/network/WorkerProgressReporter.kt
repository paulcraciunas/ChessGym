package com.paulcraciunas.data.network

import javax.inject.Inject

private typealias Reporter = suspend (Int) -> Unit

class WorkerProgressReporter @Inject constructor() {
    private lateinit var report: Reporter

    private var progress: Int = 0
    private var downloadSize: Int = 0
    private var totalDownloaded: Int = 0
    private var insertSize: Int = 0
    private var totalInserted: Int = 0

    fun init(reporter: Reporter) {
        report = reporter
        progress = 0
        downloadSize = 0
        totalDownloaded = 0
    }

    fun onBeginDownload(downloadSize: Int) {
        this.downloadSize = downloadSize
    }

    suspend fun onDownloaded(bytes: Int) {
        totalDownloaded += bytes

        update(((totalDownloaded / downloadSize.toFloat()) * Weights.Download.amount).toInt())
    }

    suspend fun onDecompressing() {
        update(progress + Weights.Decompress.amount / 2)
    }

    suspend fun onDecompressDone() {
        update(progress + Weights.Decompress.amount / 2)
    }

    fun onBeginInsert(insertCount: Int) {
        insertSize = insertCount
    }

    suspend fun onInserted(entries: Int) {
        totalInserted += entries

        update(((totalInserted / insertSize.toFloat()) * Weights.DbWrite.amount).toInt())
    }

    private suspend fun update(newProgress: Int) {
        if (newProgress != progress) {
            progress = newProgress
            report(progress)
        }
    }

    private enum class Weights(val amount: Int) {
        Download(80),
        Decompress(2),
        DbWrite(18)
    }
}
