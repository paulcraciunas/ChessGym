package com.paulcraciunas.game.engine.impl

/**
 * Low-level JNI bridge to the Stockfish native library.
 * Manages stdin/stdout pipe communication with the Stockfish UCI engine.
 */
internal class StockfishBridge {
    external fun nativeStartEngine()
    external fun nativeSendCommand(cmd: String)
    external fun nativeReadOutput(): String

    companion object {
        init {
            System.loadLibrary("stockfishjni")
        }
    }
}
