package com.paulcraciunas.data.api

import androidx.room.Database
import androidx.room.RoomDatabase
import com.paulcraciunas.data.db.puzzle.Puzzle
import com.paulcraciunas.data.db.puzzle.PuzzleDao
import com.paulcraciunas.data.db.puzzle.PuzzleThemeCrossRef
import com.paulcraciunas.data.db.rating.Rating
import com.paulcraciunas.data.db.rating.RatingDao
import com.paulcraciunas.data.db.theme.Theme
import com.paulcraciunas.data.db.theme.ThemeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [Rating::class, Theme::class, Puzzle::class, PuzzleThemeCrossRef::class],
    version = 1
)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun ratingDao(): RatingDao
    abstract fun themeDao(): ThemeDao
    abstract fun puzzleDao(): PuzzleDao

    // TODO Paul: code review this
    // TODO Paul: Unit test this
    suspend fun insertFromList(puzzleData: List<String>) = withContext(Dispatchers.IO) {
        if (puzzleData.size != 4) return@withContext

        val (fen, moves, ratingStr, themesStr) = puzzleData
        val ratingValue = ratingStr.toInt()
        val themeNames = themesStr.split(" ")

        val rDao = ratingDao()
        val tDao = themeDao()
        val pDao = puzzleDao()

        var rating = rDao.get(ratingValue)
        if (rating == null) {
            rDao.insert(Rating(value = ratingValue)).toInt()
            rating = Rating(value = ratingValue)
        }

        val puzzleId = pDao.insert(
            Puzzle(
                fen = fen,
                moves = moves,
                ratingId = rating.value
            )
        ).toInt()

        themeNames.forEach { themeName ->
            var theme = tDao.getByName(themeName)
            if (theme == null) {
                val id = tDao.insert(Theme(theme = themeName)).toInt()
                theme = Theme(id = id, theme = themeName)
            }
            pDao.insertCrossRef(
                PuzzleThemeCrossRef(
                    puzzleId = puzzleId,
                    themeId = theme.id
                )
            )
        }
    }
}
