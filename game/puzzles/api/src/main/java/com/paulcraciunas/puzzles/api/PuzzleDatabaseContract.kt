package com.paulcraciunas.puzzles.api

/**
 * Single source of truth for puzzle database naming conventions.
 *
 * These values are shared across:
 *  - Room database configuration ([ROOM_DATABASE_NAME])
 *  - The download URL construction ([databaseFileName], [compressedFileName])
 *  - The build tool that generates tiered databases ([Tier] identifiers)
 *  - The bundled Lite asset path ([LITE_ASSET_PATH])
 *
 * Changing any of these values requires coordinating:
 *  1. RoomConfigurationModule / TestDatabaseModule
 *  2. PuzzleSyncWorker (places the downloaded .db at Room's path)
 *  3. LichessDatabaseSource (constructs the download URL)
 *  4. The puzzle-db-builder tool and GitHub Action
 */
object PuzzleDatabaseContract {
    const val ROOM_DATABASE_NAME = "puzzle_database"
    const val LITE_ASSET_PATH = "databases/puzzles-lite.db"

    private const val FILE_PREFIX = "puzzles"
    private const val DB_EXTENSION = "db"
    private const val COMPRESSED_EXTENSION = "db.zst"

    object Tier {
        const val FULL = "full"
        const val COMPACT = "compact"
        const val LITE = "lite"
    }

    fun databaseFileName(tierSegment: String): String =
        "$FILE_PREFIX-$tierSegment.$DB_EXTENSION"

    fun compressedFileName(tierSegment: String): String =
        "$FILE_PREFIX-$tierSegment.$COMPRESSED_EXTENSION"
}
