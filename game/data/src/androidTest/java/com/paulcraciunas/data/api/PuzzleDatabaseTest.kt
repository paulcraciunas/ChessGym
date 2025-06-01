package com.paulcraciunas.data.api

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.data.db.theme.Theme
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

// TODO Paul: This needs to be severely expanded. We need LOADS of tests to make sure all is good
@RunWith(AndroidJUnit4::class)
class PuzzleDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val underTest = Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        PuzzleDatabase::class.java
    ).allowMainThreadQueries().build()

    @Test
    fun when_database_is_empty_then_fetching_returns_nothing() = runBlocking {
        assertTrue(underTest.themeDao().getAll().isEmpty())
        assertTrue(underTest.puzzleDao().getRandomPuzzles(1).isEmpty())
    }

    @Test
    fun when_puzzle_is_inserted_then_it_can_be_fetched() = runBlocking {
        val data = listOf(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            "e2e4 e7e5",
            "1200",
            "opening tactic"
        )
        underTest.insertFromList(data)

        val puzzles = underTest.puzzleDao().getRandomPuzzles(1)
        assertTrue(puzzles.isNotEmpty())
        assertEquals("1200", underTest.ratingDao().get(1200)?.value.toString())
        assertTrue(puzzles[0].themes.any { it.theme == "opening" })
    }

    @Test
    fun when_fetching_puzzle_by_theme_then_return_puzzle() = runBlocking {
        val data = listOf("8/8/8/8/8/8/8/8 w - - 0 1", "a1a2", "1500", "endgame fork")
        underTest.insertFromList(data)

        val puzzles = underTest.puzzleDao().getRandomPuzzlesByTheme("fork", 1)
        assertTrue(puzzles.isNotEmpty())
    }

    @Test
    fun when_fetching_all_themes_then_return_all() = runBlocking {
        underTest.themeDao().insert(Theme(theme = "attack"))
        underTest.themeDao().insert(Theme(theme = "defense"))
        underTest.themeDao().insert(Theme(theme = "sacrifice"))

        val allThemes = underTest.themeDao().getAll()
        assertEquals(3, allThemes.size)
        assertTrue(allThemes.any { it.theme == "attack" })
        assertTrue(allThemes.any { it.theme == "defense" })
        assertTrue(allThemes.any { it.theme == "sacrifice" })
    }

    @Test
    fun when_duplicate_themes_are_inserted_then_only_one_is_present() = runBlocking {
        val firstInsert = underTest.themeDao().insert(Theme(theme = "pin"))
        val secondInsert = underTest.themeDao().insert(Theme(theme = "pin"))

        val themes = underTest.themeDao().getAll()
        assertEquals(1, themes.count { it.theme == "pin" })
        assertTrue(firstInsert == secondInsert || secondInsert == -1L)
    }

    @Test
    fun when_puzzle_has_multiple_themes_then_return_them_all() = runBlocking {
        val puzzleData = listOf("8/8/8/8/8/8/8/8 w - - 0 1", "b1b3 b8b6", "1400", "pin fork double")
        underTest.insertFromList(puzzleData)
        val puzzle = underTest.puzzleDao().getRandomPuzzles(1).first()
        assertEquals(3, puzzle.themes.size)
    }
}