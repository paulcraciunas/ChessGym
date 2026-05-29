package com.paulcraciunas.screens.common.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.data.AnimatingPiece
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.SquareViewData
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import kotlin.math.roundToInt

// Most of the time, this will be the same as player's side
enum class BoardOrientation(val ranks: Array<Rank>, val files: Array<File>) {
    White(ranks = Rank.entries.reversed().toTypedArray(), files = File.entries.toTypedArray()),
    Black(ranks = Rank.entries.toTypedArray(), files = File.entries.reversed().toTypedArray());

    companion object {
        fun fromSide(player: Side): BoardOrientation = if (player == Side.WHITE) White else Black
    }
}

const val PIECE_MOVE_ANIMATION_DURATION_MS = 200

@Composable
fun ChessBoard(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    modifier: Modifier = Modifier,
    piecesAlpha: Float = 1f,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val borderSize = Design.dimensions.sizes.chessBoardBorder
    if (LocalUiSettings.current.showBorders) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(color = Design.colors.boardEdge),
            contentAlignment = Alignment.Center
        ) {
            BorderRanks(orientation = orientation, modifier = Modifier.align(Alignment.TopStart), width = borderSize)
            BorderFiles(orientation = orientation, modifier = Modifier.align(Alignment.TopCenter), height = borderSize)
            ChessBoardWithAnimation(
                board = board,
                orientation = orientation,
                onClick = onClick,
                piecesAlpha = piecesAlpha,
                modifier = Modifier.padding(borderSize)
            ) {
                overlay()
            }
            BorderFiles(orientation = orientation, modifier = Modifier.align(Alignment.BottomCenter), height = borderSize)
            BorderRanks(orientation = orientation, modifier = Modifier.align(Alignment.TopEnd), width = borderSize)
        }
    } else {
        ChessBoardWithAnimation(
            board = board,
            orientation = orientation,
            onClick = onClick,
            piecesAlpha = piecesAlpha,
            modifier = modifier,
        ) {
            overlay()
        }
    }
}

@Composable
private fun ChessBoardWithAnimation(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    piecesAlpha: Float,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    var boardWidthPx by remember { mutableIntStateOf(0) }
    val activeAnimatingPiece = if (LocalUiSettings.current.enableAnimations) board.animatingPiece else null
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onGloballyPositioned { coordinates ->
                // Capture the exact layout width allocation in raw pixels
                boardWidthPx = coordinates.size.width
            }
    ) {
        // 1. Draw your 8x8 background board layout grid using .weight() here
        ChessBoardContents(
            board = board,
            orientation = orientation,
            onClick = onClick,
            animatingPiece = activeAnimatingPiece,
            piecesAlpha = piecesAlpha,
            modifier = Modifier
        )

        // 2. Overlay the moving piece if an animation asset is running
        if (activeAnimatingPiece != null && boardWidthPx > 0) {
            AnimatedPieceOverlay(
                animatingPiece = activeAnimatingPiece,
                orientation = orientation,
                boardWidthPx = boardWidthPx,
            )
        }
        overlay()
    }
}

@Composable
private fun AnimatedPieceOverlay(
    animatingPiece: AnimatingPiece,
    orientation: BoardOrientation,
    boardWidthPx: Int,
) {
    // 1. Precise sub-pixel square allocation calculation
    val squareSizePx = boardWidthPx / 8f

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

    // 2. Map coordinates directly to a float-based Animatable vector space
    val fromOffset = Offset(
        x = fromFileIndex * squareSizePx,
        y = fromRankIndex * squareSizePx
    )
    val toOffset = Offset(
        x = toFileIndex * squareSizePx,
        y = toRankIndex * squareSizePx
    )

    // Using Offset.VectorConverter permits true diagnostic sub-pixel smooth sliding
    val animatedOffset = remember { Animatable(fromOffset, Offset.VectorConverter) }
    var currentAnimationKey by remember { mutableStateOf<AnimatingPiece?>(null) }

    LaunchedEffect(animatingPiece) {
        if (currentAnimationKey != animatingPiece) {
            currentAnimationKey = animatingPiece
            animatedOffset.snapTo(fromOffset)
            animatedOffset.animateTo(
                targetValue = toOffset,
                animationSpec = tween(durationMillis = PIECE_MOVE_ANIMATION_DURATION_MS)
            )
        }
    }

    Box(
        modifier = Modifier
            .size(with(LocalDensity.current) { squareSizePx.toDp() })
            .offset {
                IntOffset(
                    x = animatedOffset.value.x.roundToInt(),
                    y = animatedOffset.value.y.roundToInt()
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        ChessPiece(
            piece = animatingPiece.piece.piece,
            side = animatingPiece.piece.side,
        )
    }
}

@Composable
private fun ChessBoardContents(
    board: BoardViewData,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    animatingPiece: AnimatingPiece?,
    piecesAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        for (rank in orientation.ranks) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (file in orientation.files) {
                    val loc = Locus.from(file, rank)
                    val square = board.at(loc)
                    val isAnimatingTo = animatingPiece?.to == loc
                    val handleSquareClick = remember(loc, onClick) { { onClick(loc) } }

                    BoardSquare(
                        side = loc.side,
                        selected = square.highlightable,
                        highlight = square.lastMove,
                        content = {
                            if (!isAnimatingTo) {
                                SquareContent(square = square, piecesAlpha = piecesAlpha)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable(onClick = handleSquareClick)
                            .testTag { ChessBoardTags.square(file, rank) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareScope.SquareContent(
    square: SquareViewData,
    piecesAlpha: Float,
) {
    val pieceData = square.piece
    if (pieceData != null) {
        Piece(
            piece = pieceData.piece,
            alpha = piecesAlpha,
        )
    } else if (square.canMoveTo && LocalUiSettings.current.highlightLegalMoves) {
        MoveIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun WhitePerspectivePreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default().copy(showBorders = false)) {
            ChessBoard(
                board = BoardViewData.default(),
                orientation = BoardOrientation.White,
                onClick = { _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlackPerspectivePreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default().copy(showBorders = false)) {
            ChessBoard(
                board = BoardViewData.default(),
                orientation = BoardOrientation.Black,
                onClick = { _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WhitePerspectiveBordersPreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalUiSettings provides UiSettings.default()) {
            ChessBoard(
                board = BoardViewData.default(),
                orientation = BoardOrientation.White,
                onClick = { _ -> },
            )
        }
    }
}
