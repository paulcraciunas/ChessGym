package com.paulcraciunas.domain.api.blindmode

class FakeOnBlindModeGameComplete : OnBlindModeGameComplete {
    var lastResult: BlindModeGameResult? = null
        private set

    override suspend fun invoke(result: BlindModeGameResult) {
        lastResult = result
    }
}
