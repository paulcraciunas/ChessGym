package com.paulcraciunas.domain.api.achievements

enum class Achievement(
    private vararg val thresholds: Long,
) {
    RATED_PUZZLES_SOLVED(5L, 25L, 100L, 250L, 1000L),
    PUZZLE_RUSH_SESSIONS(5L, 25L, 100L, 250L, 1000L),
    STREAK_SESSIONS(5L, 10L, 25L, 50L, 100L),
    FAILED_PUZZLES_REDEEMED(5L, 25L, 100L, 250L, 1000L),
    FIND_SQUARE_SESSIONS(5L, 25L, 100L, 250L, 1000L),
    MOVE_PIECE_SESSIONS(5L, 25L, 100L, 250L, 1000L),
    BLIND_MODE_WINS(1L, 5L, 10L, 25L, 50L),
    RATED_WIN_STREAK(3L, 5L, 10L, 15L, 25L),
    RATING_CLIMBER(1300L, 1500L, 1750L, 2000L, 2250L),
    BLIND_STRATEGIST(500L, 700L, 1000L, 1300L, 1600L),
    RUSH_CHAMPION(10L, 20L, 30L, 50L, 75L),
    STREAK_LEGEND(5L, 10L, 25L, 50L, 100L),
    EAGLE_EYE(5L, 10L, 15L, 25L, 50L),
    KNIGHTS_PATH(5L, 10L, 15L, 20L, 30L),
    PUZZLE_ADDICT(50L, 250L, 500L, 1000L, 2500L),
    TIME_INVESTED(1L, 10L, 50L, 100L, 500L),
    DAILY_GRINDER(3L, 7L, 21L, 60L, 240L),
    CONSISTENCY_KING(3L, 7L, 14L, 30L, 100L),
    BOARD_VISION(10L, 50L, 200L, 500L, 2000L),
    RUSH_SOLVER(25L, 100L, 250L, 1000L, 5000L);

    init {
        require(thresholds.size == Tier.entries.size) {
            "Achievement $name must have exactly ${Tier.entries.size} thresholds."
        }
    }

    enum class Tier {
        ONE, TWO, THREE, FOUR, FIVE;

        val number: Int get() = ordinal + 1

        companion object {
            fun maxTier(): Tier = FIVE
        }
    }

    /** Returns the highest tier reached for the given [progress]. */
    fun tierFrom(progress: Long): Tier? {
        val index = thresholds.indexOfLast { progress >= it }
        return Tier.entries.getOrNull(index)
    }

    /** Returns the highest newly unlocked [Tier] when progress changes, or null if unchanged. */
    fun highestNewTier(oldProgress: Long, newProgress: Long): Tier? {
        val oldTier = tierFrom(oldProgress)
        val newTier = tierFrom(newProgress)
        return if (newTier != null && (oldTier == null || newTier > oldTier)) newTier else null
    }

    /** Returns the threshold for the tier following [currentTier]. */
    fun nextTierProgress(currentTier: Tier?): Long? {
        val nextOrdinal = (currentTier?.ordinal ?: -1) + 1
        return thresholds.getOrNull(nextOrdinal)
    }

    fun completeProgress(): Long = thresholds.last()
}
