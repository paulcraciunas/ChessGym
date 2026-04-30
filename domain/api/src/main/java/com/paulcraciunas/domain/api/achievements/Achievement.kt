package com.paulcraciunas.domain.api.achievements

enum class Achievement(
    val group: Group,
    private vararg val thresholds: Long,
) {
    RATED_PUZZLES_SOLVED(Group.ACTIVITY, 5L, 25L, 100L, 250L, 1000L),
    PUZZLE_RUSH_SESSIONS(Group.ACTIVITY, 5L, 25L, 100L, 250L, 1000L),
    STREAK_SESSIONS(Group.ACTIVITY, 5L, 10L, 25L, 50L, 100L),
    FAILED_PUZZLES_REDEEMED(Group.ACTIVITY, 5L, 25L, 100L, 250L, 1000L),
    FIND_SQUARE_SESSIONS(Group.ACTIVITY, 5L, 25L, 100L, 250L, 1000L),
    MOVE_PIECE_SESSIONS(Group.ACTIVITY, 5L, 25L, 100L, 250L, 1000L),
    BLIND_MODE_WINS(Group.ACTIVITY, 1L, 5L, 10L, 25L, 50L),
    RATED_WIN_STREAK(Group.ACTIVITY, 3L, 5L, 10L, 15L, 25L),
    RATING_CLIMBER(Group.HIGH_SCORES, 1300L, 1500L, 1750L, 2000L, 2250L),
    BLIND_STRATEGIST(Group.HIGH_SCORES, 500L, 700L, 1000L, 1300L, 1600L),
    RUSH_CHAMPION(Group.HIGH_SCORES, 10L, 20L, 30L, 50L, 75L),
    STREAK_LEGEND(Group.HIGH_SCORES, 5L, 10L, 25L, 50L, 100L),
    EAGLE_EYE(Group.HIGH_SCORES, 5L, 10L, 15L, 25L, 50L),
    KNIGHTS_PATH(Group.HIGH_SCORES, 5L, 10L, 15L, 20L, 30L),
    PUZZLE_ADDICT(Group.DEDICATION, 50L, 250L, 500L, 1000L, 2500L),
    TIME_INVESTED(Group.DEDICATION, 1L, 10L, 50L, 100L, 500L),
    DAILY_GRINDER(Group.DEDICATION, 3L, 7L, 21L, 60L, 240L),
    CONSISTENCY_KING(Group.DEDICATION, 3L, 7L, 14L, 30L, 100L),
    BOARD_VISION(Group.DEDICATION, 10L, 50L, 200L, 500L, 2000L),
    RUSH_SOLVER(Group.DEDICATION, 25L, 100L, 250L, 1000L, 5000L);

    init {
        require(thresholds.size == Tier.entries.size) {
            "Achievement $name must have exactly ${Tier.entries.size} thresholds."
        }
    }

    enum class Tier {
        ONE, TWO, THREE, FOUR, FIVE;

        val number: Int get() = ordinal + 1
    }

    enum class Group {
        ACTIVITY, HIGH_SCORES, DEDICATION,
    }

    /** Returns the highest tier reached for the given [progress]. */
    fun tierFrom(progress: Long): Tier? {
        val index = thresholds.indexOfLast { progress >= it }
        return Tier.entries.getOrNull(index)
    }

    /** Returns the threshold for the tier following [currentTier]. */
    fun nextTierProgress(currentTier: Tier?): Long? {
        val nextOrdinal = (currentTier?.ordinal ?: -1) + 1
        return thresholds.getOrNull(nextOrdinal)
    }
}
