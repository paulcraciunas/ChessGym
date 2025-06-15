package com.paulcraciunas.puzzles.impl.network.fakes

import com.paulcraciunas.puzzles.impl.network.source.PuzzleDatabaseSource
import java.io.ByteArrayInputStream
import java.io.InputStream

internal class FakeDatabaseSource : Failable(), PuzzleDatabaseSource {
    val testCsvContent = """
            PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags
            00008,rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3,f2f3 d8h4,1411,74,88,135,advantage,https://lichess.org/test,Italian_Game
        """.trimIndent()

    override suspend fun open(): Long {
        check()
        return testCsvContent.length.toLong()
    }

    override suspend fun read(): InputStream {
        check()
        return ByteArrayInputStream(testCsvContent.toByteArray())
    }
}
