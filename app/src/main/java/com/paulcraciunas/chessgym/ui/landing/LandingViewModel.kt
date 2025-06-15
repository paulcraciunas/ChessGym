package com.paulcraciunas.chessgym.ui.landing

import androidx.lifecycle.ViewModel
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//TODO Paul: Implement it properly
@HiltViewModel
class LandingViewModel @Inject constructor(
    private val fetchPuzzleDatabase: FetchPuzzleDatabase,
) : ViewModel() {
//    private val _progress = MutableLiveData(FetchPuzzleDatabase.Progress.empty())
//
//    val progress: LiveData<FetchPuzzleDatabase.Progress> = _progress
//
//    fun startPuzzleDownload() {
//        fetchPuzzleDatabase.invoke().asLiveData()
//    }
}
