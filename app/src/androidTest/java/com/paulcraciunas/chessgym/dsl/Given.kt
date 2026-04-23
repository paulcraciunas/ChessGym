package com.paulcraciunas.chessgym.dsl

import com.paulcraciunas.chessgym.di.TestApplicationSettingsModule
import com.paulcraciunas.chessgym.di.TestRandomFactoryModule
import com.paulcraciunas.chessgym.di.TestUserModule
import com.paulcraciunas.chessgym.dsl.setup.PuzzleSetup
import com.paulcraciunas.chessgym.dsl.setup.SettingsSetup
import com.paulcraciunas.chessgym.dsl.setup.UserSetup

object Given {
    val user = UserSetup(TestUserModule.userRepository)
    val settings = SettingsSetup(TestApplicationSettingsModule.appSettingsRepository)
    val puzzle = PuzzleSetup(TestRandomFactoryModule.randomFactory)
}
