package com.paulcraciunas.game.engine.impl.uci

@Suppress("UNCHECKED_CAST")
internal sealed class UciCommand {
    abstract fun protocol(): String
    open fun responseFactory(): ResponseFactory = ResponseFactory.DoneFactory

    object Init : UciCommand() {
        override fun protocol(): String = "uci"
        override fun responseFactory(): ResponseFactory = ResponseFactory.InitializedFactory
    }

    object IsReady : UciCommand() {
        override fun protocol(): String = "isready"
        override fun responseFactory(): ResponseFactory = ResponseFactory.ReadyFactory
    }

    object NewGame : UciCommand() {
        override fun protocol(): String = "ucinewgame"
    }

    object Quit : UciCommand() {
        override fun protocol(): String = "quit"
    }

    object Stop : UciCommand() {
        override fun protocol(): String = "stop"
    }

    object LimitElo : UciCommand() {
        override fun protocol(): String = "setoption name UCI_LimitStrength value true"
    }

    object DisableLimitStrength : UciCommand() {
        override fun protocol(): String = "setoption name UCI_LimitStrength value false"
    }

    class SetElo(val elo: Int) : UciCommand() {
        override fun protocol(): String = "setoption name UCI_Elo value $elo"
    }

    class SetMultiPV(val count: Int) : UciCommand() {
        override fun protocol(): String = "setoption name MultiPV value $count"
    }

    class SetPosition(val fen: String) : UciCommand() {
        override fun protocol(): String = "position fen $fen"
    }

    class SetMoveTime(val moveTimeMillis: Int = DEFAULT_MOVE_TIME_MS) : UciCommand() {
        override fun protocol(): String = "go movetime $moveTimeMillis"
        override fun responseFactory(): ResponseFactory = ResponseFactory.BestMoveFactory
    }

    class GoDepth(val depth: Int) : UciCommand() {
        override fun protocol(): String = "go depth $depth"
        override fun responseFactory(): ResponseFactory = ResponseFactory.EvaluatedBestMoveFactory()
    }

    object GoInfinite : UciCommand() {
        override fun protocol(): String = "go infinite"
    }

    companion object {
        private const val DEFAULT_MOVE_TIME_MS = 1000
    }
}
