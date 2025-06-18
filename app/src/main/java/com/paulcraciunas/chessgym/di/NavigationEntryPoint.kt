package com.paulcraciunas.chessgym.di

import com.paulcraciunas.chessgym.navigation.Navigator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NavigationEntryPoint {
    fun navigator(): Navigator
}
