package com.paulcraciunas.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcraciunas.domain.api.achievements.Achievement
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.screens.about.ui.DonationDialog
import com.paulcraciunas.screens.achievements.ui.AchievementDetailDialog
import com.paulcraciunas.screens.achievements.vm.AchievementsUiState.AchievementState
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.AbandonConfirmationType
import com.paulcraciunas.screens.common.dialogs.DeleteAccountConfirmationDialog
import com.paulcraciunas.screens.common.dialogs.PromotionDialog
import com.paulcraciunas.screens.common.dialogs.SignOutConfirmationDialog
import com.paulcraciunas.screens.loading.ui.CrashReportingConsentDialog
import com.paulcraciunas.screens.loading.ui.DownloadConfirmationDialog
import com.paulcraciunas.screens.puzzles.failed.ui.FailedPuzzlesCompletionDialog
import com.paulcraciunas.screens.puzzles.rush.ui.RushSummaryDialog
import com.paulcraciunas.screens.puzzles.streak.ui.StreakSummaryDialog

private enum class DialogEntry(val label: String, val section: String) {
    SignOut("Sign Out", "Common"),
    DeleteAccount("Delete Account", "Common"),
    AbandonPuzzle("Abandon Puzzle", "Common"),
    AbandonGame("Abandon Game", "Common"),
    PromotionWhite("Promotion (White)", "Common"),
    PromotionBlack("Promotion (Black)", "Common"),

    DownloadConfirmation("Download Confirmation", "Loading"),
    CrashReportingConsent("Crash Reporting Consent", "Loading"),

    AchievementDetail("Achievement Detail", "Achievements"),

    AboutDonation("Donation Dialog", "About"),

    RushSummary("Rush Summary", "Puzzles"),
    RushSummaryHighScore("Rush Summary (High Score)", "Puzzles"),
    StreakSummary("Streak Summary", "Puzzles"),
    StreakSummaryHighScore("Streak Summary (High Score)", "Puzzles"),
    FailedPuzzlesCompletion("Failed Puzzles Completion", "Puzzles"),
}

@Composable
fun DialogPreviewScreen() {
    var activeDialog: DialogEntry? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Design.colors.bg,
    ) { innerPadding ->
        DialogList(
            innerPadding = innerPadding,
            onDialogSelected = { activeDialog = it },
        )
    }

    activeDialog?.let { entry ->
        val onDismiss = {
            @Suppress("AssignedValueIsNeverRead") // yes it is. You're drunk
            activeDialog = null
        }
        DialogContent(entry = entry, onDismiss = onDismiss)
    }
}

@Composable
private fun DialogList(
    innerPadding: PaddingValues,
    onDialogSelected: (DialogEntry) -> Unit,
) {
    val groupedEntries = remember { DialogEntry.entries.groupBy { it.section } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        groupedEntries.forEach { (section, entries) ->
            item(key = "header_$section") {
                SectionHeader(title = section)
            }
            items(items = entries, key = { it.name }) { entry ->
                DialogButton(
                    label = entry.label,
                    onClick = { onDialogSelected(entry) },
                )
            }
            item(key = "spacer_$section") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Design.colors.ink,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun DialogButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = label)
    }
}

@Composable
private fun DialogContent(
    entry: DialogEntry,
    onDismiss: () -> Unit,
) {
    when (entry) {
        DialogEntry.SignOut -> SignOutConfirmationDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
        )
        DialogEntry.DeleteAccount -> DeleteAccountConfirmationDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
        )
        DialogEntry.AbandonPuzzle -> AbandonConfirmationDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
            type = AbandonConfirmationType.Puzzle,
        )
        DialogEntry.AbandonGame -> AbandonConfirmationDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
            type = AbandonConfirmationType.Game,
        )
        DialogEntry.PromotionWhite -> PromotionDialog(
            side = Side.WHITE,
            onPieceChosen = { onDismiss() },
        )
        DialogEntry.PromotionBlack -> PromotionDialog(
            side = Side.BLACK,
            onPieceChosen = { onDismiss() },
        )
        DialogEntry.DownloadConfirmation -> DownloadConfirmationDialog(
            approximateSize = "~ 50 MB",
            onCancelled = onDismiss,
            onConfirmed = onDismiss,
        )
        DialogEntry.CrashReportingConsent -> CrashReportingConsentDialog(
            onAccepted = onDismiss,
            onDeclined = onDismiss,
        )
        DialogEntry.AchievementDetail -> AchievementDetailDialog(
            achievementState = AchievementState.Earned(
                achievement = Achievement.RATED_PUZZLES_SOLVED,
                unseen = false,
                currentTier = Achievement.Tier.ONE,
                currentProgress = 3,
                nextThreshold = 5,
            ),
            onDismiss = onDismiss,
        )
        DialogEntry.AboutDonation -> DonationDialog(
            onDismiss = onDismiss,
            onAmountSelected = { onDismiss() }
        )
        DialogEntry.RushSummary -> RushSummaryDialog(
            puzzlesSolved = 12,
            isNewHighScore = false,
            onDismiss = onDismiss,
        )
        DialogEntry.RushSummaryHighScore -> RushSummaryDialog(
            puzzlesSolved = 19,
            isNewHighScore = true,
            onDismiss = onDismiss,
        )
        DialogEntry.StreakSummary -> StreakSummaryDialog(
            streakCount = 8,
            isNewHighScore = false,
            onDismiss = onDismiss,
        )
        DialogEntry.StreakSummaryHighScore -> StreakSummaryDialog(
            streakCount = 15,
            isNewHighScore = true,
            onDismiss = onDismiss,
        )
        DialogEntry.FailedPuzzlesCompletion -> FailedPuzzlesCompletionDialog(
            puzzlesSolved = 8,
            onDismiss = onDismiss,
        )
    }
}
