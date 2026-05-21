package com.paulcraciunas.chessgym.dsl

import com.paulcraciunas.chessgym.di.TestApplicationSettingsModule
import com.paulcraciunas.chessgym.di.TestClockTimersModule
import com.paulcraciunas.chessgym.di.TestNetworkModule
import com.paulcraciunas.chessgym.di.TestRandomFactoryModule
import com.paulcraciunas.chessgym.di.TestUserModule
import com.paulcraciunas.chessgym.dsl.setup.AuthSetup
import com.paulcraciunas.chessgym.dsl.setup.ClockSetup
import com.paulcraciunas.chessgym.dsl.setup.PuzzleSetup
import com.paulcraciunas.chessgym.dsl.setup.SettingsSetup
import com.paulcraciunas.chessgym.dsl.setup.UserSetup

object Given {
    val user = UserSetup(TestUserModule.userRepository)
    val auth = AuthSetup(TestNetworkModule.authService)
    val settings = SettingsSetup(TestApplicationSettingsModule.appSettingsRepository)
    val puzzle = PuzzleSetup(TestRandomFactoryModule.randomFactory)
    val clock = ClockSetup(
        TestClockTimersModule.whiteTimer,
        TestClockTimersModule.blackTimer,
        TestClockTimersModule.defaultTimer,
    )
}
