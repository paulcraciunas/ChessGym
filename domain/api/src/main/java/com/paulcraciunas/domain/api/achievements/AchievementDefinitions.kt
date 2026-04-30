package com.paulcraciunas.domain.api.achievements

enum class Tier {
    ONE, TWO, THREE, FOUR, FIVE;

    val number: Int get() = ordinal + 1
}

enum class AchievementGroup {
    ACTIVITY, HIGH_SCORES, DEDICATION,
}

enum class AchievementCategory(val group: AchievementGroup) {
    RATED_PUZZLES_SOLVED(AchievementGroup.ACTIVITY),
    PUZZLE_RUSH_SESSIONS(AchievementGroup.ACTIVITY),
    STREAK_SESSIONS(AchievementGroup.ACTIVITY),
    FAILED_PUZZLES_REDEEMED(AchievementGroup.ACTIVITY),
    FIND_SQUARE_SESSIONS(AchievementGroup.ACTIVITY),
    MOVE_PIECE_SESSIONS(AchievementGroup.ACTIVITY),
    BLIND_MODE_WINS(AchievementGroup.ACTIVITY),
    RATED_WIN_STREAK(AchievementGroup.ACTIVITY),
    RATING_CLIMBER(AchievementGroup.HIGH_SCORES),
    BLIND_STRATEGIST(AchievementGroup.HIGH_SCORES),
    RUSH_CHAMPION(AchievementGroup.HIGH_SCORES),
    STREAK_LEGEND(AchievementGroup.HIGH_SCORES),
    EAGLE_EYE(AchievementGroup.HIGH_SCORES),
    KNIGHTS_PATH(AchievementGroup.HIGH_SCORES),
    PUZZLE_ADDICT(AchievementGroup.DEDICATION),
    TIME_INVESTED(AchievementGroup.DEDICATION),
    DAILY_GRINDER(AchievementGroup.DEDICATION),
    CONSISTENCY_KING(AchievementGroup.DEDICATION),
    BOARD_VISION(AchievementGroup.DEDICATION),
    RUSH_SOLVER(AchievementGroup.DEDICATION),
}

data class AchievementTier(
    val tier: Tier,
    val threshold: Long,
)

data class AchievementDefinition(
    val category: AchievementCategory,
    val tiers: List<AchievementTier>,
)

private val THRESHOLDS: Map<AchievementCategory, List<Long>> = mapOf(
    AchievementCategory.RATED_PUZZLES_SOLVED to listOf(5, 25, 100, 250, 1000),
    AchievementCategory.PUZZLE_RUSH_SESSIONS to listOf(5, 25, 100, 250, 1000),
    AchievementCategory.STREAK_SESSIONS to listOf(5, 10, 25, 50, 100),
    AchievementCategory.FAILED_PUZZLES_REDEEMED to listOf(5, 25, 100, 250, 1000),
    AchievementCategory.FIND_SQUARE_SESSIONS to listOf(5, 25, 100, 250, 1000),
    AchievementCategory.MOVE_PIECE_SESSIONS to listOf(5, 25, 100, 250, 1000),
    AchievementCategory.BLIND_MODE_WINS to listOf(1, 5, 10, 25, 50),
    AchievementCategory.RATED_WIN_STREAK to listOf(3, 5, 10, 15, 25),
    AchievementCategory.RATING_CLIMBER to listOf(1300, 1500, 1750, 2000, 2250),
    AchievementCategory.BLIND_STRATEGIST to listOf(500, 700, 1000, 1300, 1600),
    AchievementCategory.RUSH_CHAMPION to listOf(10, 20, 30, 50, 75),
    AchievementCategory.STREAK_LEGEND to listOf(5, 10, 25, 50, 100),
    AchievementCategory.EAGLE_EYE to listOf(5, 10, 15, 25, 50),
    AchievementCategory.KNIGHTS_PATH to listOf(5, 10, 15, 20, 30),
    AchievementCategory.PUZZLE_ADDICT to listOf(50, 250, 500, 1000, 2500),
    AchievementCategory.TIME_INVESTED to listOf(1, 10, 50, 100, 500),
    AchievementCategory.DAILY_GRINDER to listOf(3, 7, 21, 60, 240),
    AchievementCategory.CONSISTENCY_KING to listOf(3, 7, 14, 30, 100),
    AchievementCategory.BOARD_VISION to listOf(10, 50, 200, 500, 2000),
    AchievementCategory.RUSH_SOLVER to listOf(25, 100, 250, 1000, 5000),
)

val ALL_ACHIEVEMENTS: List<AchievementDefinition> = AchievementCategory.entries.map { category ->
    val thresholds = THRESHOLDS[category] ?: throw IllegalStateException("Missing thresholds for $category")
    AchievementDefinition(
        category = category,
        tiers = thresholds.sorted().mapIndexed { index, threshold ->
            AchievementTier(Tier.entries[index], threshold)
        }
    )
}

private val achievementsByCategory: Map<AchievementCategory, AchievementDefinition> =
    ALL_ACHIEVEMENTS.associateBy { it.category }

fun AchievementCategory.definition(): AchievementDefinition =
    achievementsByCategory.getValue(this)

fun AchievementCategory.currentTier(progressValue: Long): Tier? =
    definition().tiers.lastOrNull { progressValue >= it.threshold }?.tier

fun AchievementCategory.nextTierThreshold(currentTier: Tier?): Long? {
    val nextIndex = (currentTier?.ordinal ?: -1) + 1
    return definition().tiers.getOrNull(nextIndex)?.threshold
}
