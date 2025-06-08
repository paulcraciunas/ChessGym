package com.paulcraciunas.serializer.impl.binary

import com.paulcraciunas.game.logic.api.CastleType
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource

internal class BinaryAdapterTest {
    private val underTest = BinaryAdapter()

    @ParameterizedTest(name = "White {0} is serialized to binary and back")
    @MethodSource("pieces")
    fun `WHEN white piece is adapted to binary THEN binary can be adapted back`(piece: Piece) {
        val binary = underTest.toBinary(BinaryAdapter.SidedPiece(piece, Side.WHITE))
        val data = underTest.toPiece(binary)

        assertEquals(Side.WHITE, data.side)
        assertEquals(piece, data.piece)
    }

    @ParameterizedTest(name = "Black {0} is serialized to binary and back")
    @MethodSource("pieces")
    fun `WHEN black piece is adapted to binary THEN binary can be adapted back`(piece: Piece) {
        val binary = underTest.toBinary(BinaryAdapter.SidedPiece(piece, Side.BLACK))
        val data = underTest.toPiece(binary)

        assertEquals(Side.BLACK, data.side)
        assertEquals(piece, data.piece)
    }

    @Test
    fun `WHEN location is adapted to binary THEN binary can be adapted back`() {
        Locus.all {
            val binary = underTest.toBinary(it)
            val data = underTest.toLocation(binary)

            assertEquals(it, data)
        }
    }

    @ParameterizedTest(name = "Castling {0} is serialized to binary and back")
    @MethodSource("castling")
    fun `WHEN castling is adapted to binary THEN binary can be adapted back`(
        white: Set<CastleType>,
        black: Set<CastleType>,
    ) {
        val binary = underTest.toBinary(white, black)
        val data = underTest.toCastling(binary)

        assertEquals(white, data.first)
        assertEquals(black, data.second)
    }

    @ParameterizedTest(name = "Move {0} is serialized to binary and back")
    @MethodSource("moves")
    fun `WHEN move is adapted to binary THEN binary can be adapted back`(move: String) {
        val binary = underTest.toBinary(move)

        assertEquals(move, underTest.toMove(binary))
    }

    @ParameterizedTest(name = "Promotion move {0} is serialized to binary and back")
    @MethodSource("promotions")
    fun `WHEN promotion move is adapted to binary THEN binary can be adapted back`(move: String) {
        val binary = underTest.toBinary(move)

        assertEquals(move, underTest.toMove(binary))
    }

    companion object {
        @JvmStatic
        fun pieces(): List<Piece> = Piece.entries.toList()

        @JvmStatic
        fun castling(): List<Arguments> = listOf<Arguments>(
            of(setOf(CastleType.KingSide), setOf<CastleType>()),
            of(setOf(CastleType.QueenSide), setOf<CastleType>()),
            of(setOf<CastleType>(), setOf(CastleType.KingSide)),
            of(setOf<CastleType>(), setOf(CastleType.QueenSide)),
            of(setOf(CastleType.KingSide), setOf(CastleType.QueenSide)),
            of(setOf(CastleType.QueenSide), setOf(CastleType.KingSide)),
            of(
                setOf(CastleType.KingSide, CastleType.QueenSide),
                setOf(CastleType.QueenSide)
            ),
            of(
                setOf(CastleType.KingSide, CastleType.QueenSide),
                setOf(CastleType.KingSide, CastleType.QueenSide)
            ),
            of(setOf<CastleType>(), setOf<CastleType>()),
        )

        @JvmStatic
        fun moves(): List<String> = File.entries.flatMap { file ->
            Rank.entries.map { rank ->
                "$file$rank"
            }
        }.map { "${it}h8" }

        @JvmStatic
        fun promotions(): List<String> = listOf("e7e8q", "c7c8n", "b7b8r", "a2a1b")
    }
}