package com.paulcraciunas.chessgym.startup

import com.paulcraciunas.settings.application.api.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsProvisioning {
    operator fun invoke(): Flow<AppSettings>
}
