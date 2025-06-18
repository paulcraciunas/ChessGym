package com.paulcraciunas.chessgym.di

import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.navigation.NavHostNavigator
import com.paulcraciunas.chessgym.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NavigationModule {
    @Provides
    @Singleton
    fun provideNavigator(navController: NavHostController): Navigator = NavHostNavigator(navController)
}
