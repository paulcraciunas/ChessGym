package com.paulcraciunas.puzzles.impl

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.puzzles.impl.db.Puzzle
import com.paulcraciunas.puzzles.impl.impl.PuzzleDatabase
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PuzzleDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val underTest = Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        PuzzleDatabase::class.java
    ).allowMainThreadQueries().build()

    @Test
    fun when_database_is_empty_then_fetching_returns_nothing() = runBlocking {
        TestCase.assertTrue(underTest.puzzleDao().get(1).isEmpty())
        TestCase.assertNull(underTest.puzzleDao().getByRating(100))
        TestCase.assertNull(underTest.puzzleDao().getInRange(100, 10000))
    }

    @Test
    fun when_puzzle_is_inserted_then_it_can_be_fetched() = runBlocking {
        underTest.insert(Puzzle(fenBinary = FEN, rating = 1000))
        // when
        val puzzles = underTest.get(count = 1)
        //then
        TestCase.assertEquals(1, puzzles.size)
        TestCase.assertTrue(FEN.contentEquals(puzzles[0].fenBinary))
        TestCase.assertEquals(1000, puzzles[0].rating)
    }

    @Test
    fun when_duplicate_puzzles_are_inserted_then_only_one_is_present() = runBlocking {
        val firstInsert = underTest.insert(Puzzle(fenBinary = FEN, rating = 1000))
        val secondInsert = underTest.insert(Puzzle(fenBinary = FEN, rating = 1000))
        // when
        val puzzles = underTest.get(count = 1)
        //then
        TestCase.assertEquals(1, puzzles.count { it.fenBinary.contentEquals(FEN) })
        TestCase.assertTrue(firstInsert == secondInsert || secondInsert == -1L)
    }

    @Test
    fun when_multiple_puzzles_are_inserted_then_they_can_be_fetched() = runBlocking {
        val data = List(1000) { i -> Puzzle(fenBinary = FEN, rating = 1000 + i) }
        // when
        underTest.bulkInsert(data)
        //then
        val puzzles = underTest.get(count = 10)
        TestCase.assertTrue(puzzles.isNotEmpty())
    }

    companion object {
        private val FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1,a2a3 a7a6".toByteArray()
    }
}