package com.paulcraciunas.screens.tools.importgame.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.screens.data.engine.PlayIntent
import com.paulcraciunas.screens.data.engine.SinglePlaySession
import com.paulcraciunas.screens.data.engine.SingleSessionConfiguration
import com.paulcraciunas.screens.data.engine.SingleSessionState
import com.paulcraciunas.serializer.api.SerializeException
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.di.SerializerFen
import com.paulcraciunas.serializer.di.SerializerPgn
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal data class VmState(
    val showImportDialog: ImportType? = null,
    val importType: ImportType? = null,
    val importError: String? = null,
)

@HiltViewModel
class ImportGameViewModel @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @SerializerFen fenSerializer: Serializer,
    @SerializerPgn pgnSerializer: Serializer,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val vmState = MutableStateFlow(VmState())
    private val session = ImportSession(fenSerializer, pgnSerializer)
    private val playSession = SinglePlaySession(
        settingsRepository = appSettingsRepository,
        config = SingleSessionConfiguration(gameOverBehavior = SingleSessionConfiguration.GameOverBehavior.AllowNavigation),
    )

    val uiState: StateFlow<ImportGameUiState> = combine(
        playSession.state,
        vmState
    ) { state, vmState -> state.toUiState(vmState) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ImportGameUiState()
        )

    fun onFenClicked() = vmState.update { it.copy(showImportDialog = ImportType.FEN, importError = null) }
    fun onPgnClicked() = vmState.update { it.copy(showImportDialog = ImportType.PGN, importError = null) }
    fun onDismissDialog() = vmState.update { it.copy(showImportDialog = null, importError = null) }

    fun onImport(gameString: String) {
        val importType = vmState.value.showImportDialog ?: return

        viewModelScope.launch(dispatcher) {
            try {
                session.import(gameString = gameString, of = importType)
                vmState.update { it.copy(showImportDialog = null, importType = importType, importError = null) }
                playSession.run(session.createSession())
            } catch (e: SerializeException) {
                Timber.w(e, "Failed to import game from: $gameString")
                vmState.update { it.copy(importError = e.message) }
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Failed to import game from: $gameString")
                vmState.update { it.copy(importError = e.message) }
            }
        }
    }

    fun onSquareClicked(locus: Locus) {
        if (vmState.value.importType == ImportType.PGN) return // can't play through full imported games
        playSession.accept(intent = PlayIntent.SelectSquare(locus))
    }

    fun onPromote(to: Piece) = playSession.accept(intent = PlayIntent.Promote(to))
    fun onJumpToStart() = playSession.accept(intent = PlayIntent.Navigate(PlayIntent.Navigation.ToStart))
    fun onPreviousMove() = playSession.accept(intent = PlayIntent.Navigate(PlayIntent.Navigation.Back))
    fun onNextMove() = playSession.accept(intent = PlayIntent.Navigate(PlayIntent.Navigation.Forward))
    fun onJumpToEnd() = playSession.accept(intent = PlayIntent.Navigate(PlayIntent.Navigation.ToEnd))

    private fun SingleSessionState.toUiState(vmState: VmState): ImportGameUiState = when (status) {
        SingleSessionState.Status.Loading -> ImportGameUiState(
            showImportDialog = vmState.showImportDialog,
            importType = vmState.importType,
            importError = vmState.importError,
        )
        SingleSessionState.Status.Ready,
        SingleSessionState.Status.Playing,
        SingleSessionState.Status.GameOver -> ImportGameUiState(
            data = this.boardState,
            importType = vmState.importType,
            isGameLoaded = true,
            canNavigateBack = this.navigation?.canGoBack == true,
            canNavigateForward = this.navigation?.canGoForward == true,
        )
        SingleSessionState.Status.Failed -> ImportGameUiState(
            data = this.boardState,
            importType = vmState.importType,
            isGameLoaded = false,
        )
    }
}
