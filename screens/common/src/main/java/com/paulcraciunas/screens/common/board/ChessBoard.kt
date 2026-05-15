package com.paulcraciunas.screens.common.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.common.model.AnimatingPiece
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import com.paulcraciunas.screens.common.model.SquareViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.BoardColors
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import kotlin.math.roundToInt

private val borderSize = 14.dp

// Most of the time, this will be the same as player's side
enum class BoardOrientation(val ranks: List<Rank>, val files: List<File>) {
    White(ranks = Rank.entries.reversed(), files = File.entries),
    Black(ranks = Rank.entries, files = File.entries.reversed());

    companion object {
        fun fromSide(player: Side): BoardOrientation = if (player == Side.WHITE) White else Black
    }
}

private const val ANIMATION_DURATION_MS = 200

@Composable
fun ChessBoard(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    showBorders: Boolean,
    modifier: Modifier = Modifier,
    highlightLegalMoves: Boolean = true,
    enableAnimations: Boolean = true,
    piecesAlpha: Float = 1f,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(color = BoardColors.boardEdge),
        contentAlignment = Alignment.Center
    ) {
        if (showBorders) {
            BorderRanks(orientation = orientation, modifier = Modifier.align(Alignment.TopStart), width = borderSize)
            BorderFiles(orientation = orientation, modifier = Modifier.align(Alignment.TopCenter), height = borderSize)
        }
        ChessBoardWithAnimation(
            board = board,
            orientation = orientation,
            onClick = onClick,
            highlightLegalMoves = highlightLegalMoves,
            enableAnimations = enableAnimations,
            piecesAlpha = piecesAlpha,
            modifier = modifier.padding(if (showBorders) borderSize else 0.dp)
        )
        if (showBorders) {
            BorderFiles(orientation = orientation, modifier = Modifier.align(Alignment.BottomCenter), height = borderSize)
            BorderRanks(orientation = orientation, modifier = Modifier.align(Alignment.TopEnd), width = borderSize)
        }
        overlay()
    }
}

@Composable
private fun ChessBoardWithAnimation(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    highlightLegalMoves: Boolean,
    enableAnimations: Boolean,
    piecesAlpha: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        // The modifier already has padding applied when borders are shown,
        // so maxWidth/maxHeight are already the board content size
        val squareSize = minOf(this.maxWidth, this.maxHeight) / 8
        val activeAnimatingPiece = if (enableAnimations) board.animatingPiece else null

        ChessBoardContents(
            board = board,
            orientation = orientation,
            onClick = onClick,
            highlightLegalMoves = highlightLegalMoves,
            animatingPiece = activeAnimatingPiece,
            piecesAlpha = piecesAlpha,
            modifier = Modifier
        )

        if (enableAnimations) {
            board.animatingPiece?.let { animating ->
                AnimatedPieceOverlay(
                    animatingPiece = animating,
                    orientation = orientation,
                    squareSize = squareSize,
                )
            }
        }
    }
}

@Composable
private fun AnimatedPieceOverlay(
    animatingPiece: AnimatingPiece,
    orientation: BoardOrientation,
    squareSize: Dp,
) {
    // Calculate positions based on orientation
    val fromFileIndex = when (orientation) {
        BoardOrientation.White -> animatingPiece.from.file.ordinal
        BoardOrientation.Black -> 7 - animatingPiece.from.file.ordinal
    }
    val fromRankIndex = when (orientation) {
        BoardOrientation.White -> 7 - animatingPiece.from.rank.ordinal
        BoardOrientation.Black -> animatingPiece.from.rank.ordinal
    }
    val toFileIndex = when (orientation) {
        BoardOrientation.White -> animatingPiece.to.file.ordinal
        BoardOrientation.Black -> 7 - animatingPiece.to.file.ordinal
    }
    val toRankIndex = when (orientation) {
        BoardOrientation.White -> 7 - animatingPiece.to.rank.ordinal
        BoardOrientation.Black -> animatingPiece.to.rank.ordinal
    }

    val squareSizePx = with(androidx.compose.ui.platform.LocalDensity.current) {
        squareSize.toPx()
    }

    val fromOffset = IntOffset(
        x = (fromFileIndex * squareSizePx).roundToInt(),
        y = (fromRankIndex * squareSizePx).roundToInt()
    )
    val toOffset = IntOffset(
        x = (toFileIndex * squareSizePx).roundToInt(),
        y = (toRankIndex * squareSizePx).roundToInt()
    )

    // Track which animation we're running to restart on new animation
    var currentAnimationKey by remember { mutableStateOf<AnimatingPiece?>(null) }
    val animatedOffset = remember { Animatable(fromOffset, IntOffset.VectorConverter) }

    LaunchedEffect(animatingPiece) {
        if (currentAnimationKey != animatingPiece) {
            currentAnimationKey = animatingPiece
            animatedOffset.snapTo(fromOffset)
            animatedOffset.animateTo(
                targetValue = toOffset,
                animationSpec = tween(durationMillis = ANIMATION_DURATION_MS)
            )
        }
    }

    Box(
        modifier = Modifier
            .offset { animatedOffset.value }
            .size(squareSize),
        contentAlignment = Alignment.Center
    ) {
        ChessPiece(
            piece = animatingPiece.piece,
            side = animatingPiece.side,
        )
    }
}

@Composable
private fun ChessBoardContents(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    highlightLegalMoves: Boolean,
    animatingPiece: AnimatingPiece?,
    piecesAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        for (rank in orientation.ranks) {
            Row {
                for (file in orientation.files) {
                    val square = board.at(rank, file)
                    val squareSide = squareSide(file, rank)
                    val isAnimatingTo = animatingPiece?.to?.let {
                        it.file == file && it.rank == rank
                    } ?: false
                    BoardSquare(
                        side = squareSide,
                        highlight = square.lastMove,
                        content = {
                            if (isAnimatingTo) {
                                Plain()
                            } else {
                                SquareContent(square, highlightLegalMoves, piecesAlpha)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .testTag { ChessBoardTags.square(file, rank) }
                            .clickable { onClick(Locus(file, rank)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareScope.SquareContent(
    square: SquareViewData,
    highlightLegalMoves: Boolean,
    piecesAlpha: Float,
) {
    if (square.piece != null) {
        Piece(
            piece = square.piece.piece,
            side = square.piece.side,
            selected = square.piece.isSelected,
            alpha = piecesAlpha,
        )
    } else if (square.canMoveTo && highlightLegalMoves) {
        MoveAvailable()
    } else {
        Plain()
    }
    if (square.piece != null && square.canMoveTo && highlightLegalMoves && piecesAlpha < 1f) {
        MoveIndicatorOverlay()
    }
}

private fun squareSide(file: File, rank: Rank): Side =
    if ((rank.ordinal + file.ordinal) % 2 == 0) Side.BLACK
    else Side.WHITE

@Preview(showBackground = true)
@Composable
private fun WhitePerspectivePreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.White,
            onClick = { _ -> },
            showBorders = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlackPerspectivePreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.Black,
            onClick = { _ -> },
            showBorders = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WhitePerspectiveBordersPreview() {
    ChessGymTheme {
        ChessBoard(
            board = BoardViewDataBuilder().build(),
            orientation = BoardOrientation.White,
            onClick = { _ -> },
            showBorders = true
        )
    }
}
