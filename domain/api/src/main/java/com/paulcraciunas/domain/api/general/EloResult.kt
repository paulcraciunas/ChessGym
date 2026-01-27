package com.paulcraciunas.domain.api.general

data class EloResult(
    val potentialGain: Int,
    val potentialLoss: Int
) {
    fun get(success: Boolean) = if (success) potentialGain else potentialLoss
}
