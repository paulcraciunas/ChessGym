package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin

val junit4TestStub: TestFile = kotlin(
    """
    package org.junit
    annotation class Test
    """
).indented()

val junit5TestStub: TestFile = kotlin(
    """
    package org.junit.jupiter.api
    annotation class Test
    """
).indented()

val composableStub: TestFile = kotlin(
    """
    package androidx.compose.runtime
    annotation class Composable
    """
).indented()

@Suppress("UnusedReceiverParameter")
val coroutineScopeStub: TestFile = kotlin(
    """
    package kotlinx.coroutines
    interface CoroutineScope
    fun CoroutineScope.launch(block: suspend () -> Unit) {}
    fun CoroutineScope.async(block: suspend () -> Unit) {}
    """
).indented()

val coroutineDispatcherStub: TestFile = kotlin(
    """
    package kotlinx.coroutines
    abstract class CoroutineDispatcher
    object Dispatchers {
        val IO: CoroutineDispatcher = object : CoroutineDispatcher() {}
        val Default: CoroutineDispatcher = object : CoroutineDispatcher() {}
        val Main: CoroutineDispatcher = object : CoroutineDispatcher() {}
        val Unconfined: CoroutineDispatcher = object : CoroutineDispatcher() {}
    }
    """
).indented()

val coroutineWithContextStub: TestFile = kotlin(
    """
        package kotlinx.coroutines.withContext
        suspend fun <T> withContext(
            context: CoroutineContext,
            block: suspend CoroutineScope.() -> T
        ): T { 
            delay(4242L)
        }
    """
).indented()

val flowStub: TestFile = kotlin(
    """
    package kotlinx.coroutines.flow
    interface Flow<out T>
    """
).indented()

val daggerModuleStub: TestFile = kotlin(
    """
    package dagger
    annotation class Module
    """
).indented()
