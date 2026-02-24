package com.paulcraciunas.screens.settings.vm

import com.paulcraciunas.settings.application.api.AppSettings

interface SettingsScreenInteractor {
    fun onHapticFeedbackToggled(isEnabled: Boolean)
    fun onAutoPromoteToggled(isEnabled: Boolean)
    fun onShowBordersToggled(isEnabled: Boolean)
    fun onHighlightLegalMovesToggled(isEnabled: Boolean)
    fun onLightModeSelected(mode: AppSettings.LightMode)
    fun onAnimationsToggled(isEnabled: Boolean)
}
