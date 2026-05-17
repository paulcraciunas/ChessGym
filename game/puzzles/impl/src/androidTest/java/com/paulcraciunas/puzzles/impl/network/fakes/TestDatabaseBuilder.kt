package com.paulcraciunas.puzzles.impl.network.fakes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.serializer.impl.binary.BinaryAdapter
import com.paulcraciunas.serializer.impl.binary.BinaryPuzzleWriter
import java.io.File

private const val IDENTITY_HASH = "445cfb97105e05c40e29961f336a0498"

internal object TestDatabaseBuilder {
    private val writer = BinaryPuzzleWriter(FenSerializer(RealGameFactory()), BinaryAdapter())

    fun buildTestDatabase(context: Context, puzzles: List<TestPuzzle>): ByteArray {
        val tempFile = File(context.cacheDir, "test_build.db")
        tempFile.delete()

        SQLiteDatabase.openOrCreateDatabase(tempFile, null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS Puzzle (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "fenBinary BLOB NOT NULL, " +
                    "rating INTEGER NOT NULL)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_Puzzle_rating ON Puzzle (rating)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table (" +
                    "id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                    "VALUES(42, '$IDENTITY_HASH')"
            )

            puzzles.forEach { puzzle ->
                val binary = writer.write(puzzle.fen, puzzle.moves)
                val values = ContentValues().apply {
                    put("fenBinary", binary)
                    put("rating", puzzle.rating)
                }
                db.insert("Puzzle", null, values)
            }
        }

        val bytes = tempFile.readBytes()
        tempFile.delete()
        return bytes
    }

    data class TestPuzzle(
        val fen: String,
        val moves: String,
        val rating: Int,
    )
}
