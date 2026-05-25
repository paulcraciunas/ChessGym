package com.paulcraciunas.screens.common.board.v2

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
import androidx.compose.ui.unit.dp
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.screens.common.LocalAppSettings
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.common.board.ChessPiece
import com.paulcraciunas.screens.common.board.PIECE_MOVE_ANIMATION_DURATION_MS
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.model.v2.AnimatingPiece2
import com.paulcraciunas.screens.common.model.v2.BoardViewData2
import com.paulcraciunas.screens.common.model.v2.SquareViewData2
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.common.theme.ChessGymTheme
import com.paulcraciunas.settings.application.api.AppSettings
import kotlin.math.roundToInt

private val borderSize = 14.dp

// Most of the time, this will be the same as player's side
enum class BoardOrientation2(val ranks: Array<Rank>, val files: Array<File>) {
    White(ranks = Rank.entries.reversed().toTypedArray(), files = File.entries.toTypedArray()),
    Black(ranks = Rank.entries.toTypedArray(), files = File.entries.reversed().toTypedArray());

    companion object {
        fun fromSide(player: Side): BoardOrientation2 = if (player == Side.WHITE) White else Black
    }
}

@Composable
fun ChessBoard2(
    board: BoardViewData2,
    orientation: BoardOrientation2,
    onClick: (Locus) -> Unit,
    modifier: Modifier = Modifier,
    piecesAlpha: Float = 1f,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    if (LocalAppSettings.current.showBorders) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(color = Design.colors.boardEdge),
            contentAlignment = Alignment.Center
        ) {
            BorderRanks2(orientation = orientation, modifier = Modifier.align(Alignment.TopStart), width = borderSize)
            BorderFiles2(orientation = orientation, modifier = Modifier.align(Alignment.TopCenter), height = borderSize)
            ChessBoardWithAnimation2(
                board = board,
                orientation = orientation,
                onClick = onClick,
                piecesAlpha = piecesAlpha,
                modifier = Modifier.padding(borderSize)
            ) {
                overlay()
            }
            BorderFiles2(orientation = orientation, modifier = Modifier.align(Alignment.BottomCenter), height = borderSize)
            BorderRanks2(orientation = orientation, modifier = Modifier.align(Alignment.TopEnd), width = borderSize)
        }
    } else {
        ChessBoardWithAnimation2(
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
private fun ChessBoardWithAnimation2(
    board: BoardViewData2,
    orientation: BoardOrientation2,
    onClick: (Locus) -> Unit,
    piecesAlpha: Float,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    var boardWidthPx by remember { mutableIntStateOf(0) }
    val activeAnimatingPiece = if (LocalAppSettings.current.enableAnimations) board.animatingPiece else null
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
        ChessBoardContents2(
            board = board,
            orientation = orientation,
            onClick = onClick,
            animatingPiece = activeAnimatingPiece,
            piecesAlpha = piecesAlpha,
            modifier = Modifier
        )

        // 2. Overlay the moving piece if an animation asset is running
        if (activeAnimatingPiece != null && boardWidthPx > 0) {
            AnimatedPieceOverlay2(
                animatingPiece = activeAnimatingPiece,
                orientation = orientation,
                boardWidthPx = boardWidthPx,

                )
        }
        overlay()
    }
}

@Composable
private fun AnimatedPieceOverlay2(
    animatingPiece: AnimatingPiece2,
    orientation: BoardOrientation2,
    boardWidthPx: Int,
) {
    // 1. Precise sub-pixel square allocation calculation
    val squareSizePx = boardWidthPx / 8f

    val fromFileIndex = when (orientation) {
        BoardOrientation2.White -> animatingPiece.from.file.ordinal
        BoardOrientation2.Black -> 7 - animatingPiece.from.file.ordinal
    }
    val fromRankIndex = when (orientation) {
        BoardOrientation2.White -> 7 - animatingPiece.from.rank.ordinal
        BoardOrientation2.Black -> animatingPiece.from.rank.ordinal
    }
    val toFileIndex = when (orientation) {
        BoardOrientation2.White -> animatingPiece.to.file.ordinal
        BoardOrientation2.Black -> 7 - animatingPiece.to.file.ordinal
    }
    val toRankIndex = when (orientation) {
        BoardOrientation2.White -> 7 - animatingPiece.to.rank.ordinal
        BoardOrientation2.Black -> animatingPiece.to.rank.ordinal
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
    var currentAnimationKey by remember { mutableStateOf<AnimatingPiece2?>(null) }

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
private fun ChessBoardContents2(
    board: BoardViewData2,
    orientation: BoardOrientation2,
    onClick: (Locus) -> Unit,
    animatingPiece: AnimatingPiece2?,
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
                    val square = board.at(rank, file)
                    val squareSide = squareSide(file, rank)
                    val isAnimatingTo = animatingPiece?.to?.let {
                        it.file == file && it.rank == rank
                    } ?: false
                    BoardSquare2(
                        side = squareSide,
                        highlight = square.lastMove,
                        content = {
                            if (!isAnimatingTo) {
                                SquareContent2(square, piecesAlpha)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onClick(Locus.from(file, rank)) }
                            .testTag { ChessBoardTags.square(file, rank) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareScope2.SquareContent2(
    square: SquareViewData2,
    piecesAlpha: Float,
) {
    if (square.piece != null) {
        Piece(
            piece = square.piece.piece,
            selected = square.piece.isSelected || square.canMoveTo, // important fix to add canMoveTo
            alpha = piecesAlpha,
        )
    } else if (square.canMoveTo && LocalAppSettings.current.highlightLegalMoves) {
        MoveIndicator()
    }
    if (square.piece != null && square.canMoveTo && LocalAppSettings.current.highlightLegalMoves && piecesAlpha < 1f) {
        MoveIndicator()
    }
}

private fun squareSide(file: File, rank: Rank): Side =
    if ((rank.ordinal + file.ordinal) % 2 == 0) Side.BLACK
    else Side.WHITE

@Preview(showBackground = true)
@Composable
private fun WhitePerspectivePreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalAppSettings provides AppSettings.default().copy(showBorders = false)) {
            ChessBoard2(
                board = BoardViewData2.default(),
                orientation = BoardOrientation2.White,
                onClick = { _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlackPerspectivePreview() {
    ChessGymTheme {
        CompositionLocalProvider(LocalAppSettings provides AppSettings.default().copy(showBorders = false)) {
            ChessBoard2(
                board = BoardViewData2.default(),
                orientation = BoardOrientation2.Black,
                onClick = { _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WhitePerspectiveBordersPreview() {
    ChessGymTheme {
        ChessBoard2(
            board = BoardViewData2.default(),
            orientation = BoardOrientation2.White,
            onClick = { _ -> },
        )
    }
}
