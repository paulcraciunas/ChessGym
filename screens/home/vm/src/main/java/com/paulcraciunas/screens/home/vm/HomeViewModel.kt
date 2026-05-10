package com.paulcraciunas.screens.home.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stateAdapter: HomeUiStateAdapter,
    userRepository: UserRepository,
) : ViewModel() {
    // We'll trigger a refresh every hour. This will be noticeable at midnight, when we switch
    // user history items from "Today" to "Yesterday", etc.
    private val refreshTrigger = flow {
        while (true) {
            emit(Unit)
            delay(1.hours)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.userUpdates(),
        refreshTrigger
    ) { user, _ ->
        stateAdapter.adapt(user)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
