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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.SidedPiece
import com.paulcraciunas.screens.common.LocalUiSettings
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.screens.common.design.theme.Design
import com.paulcraciunas.screens.common.previews.ChessBoardPreviewRoot
import com.paulcraciunas.screens.common.testTag
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.PIECE_MOVE_ANIMATION_DURATION_MS
import kotlin.math.roundToInt

// Most of the time, this will be the same as player's side
enum class BoardOrientation(val ranks: Array<Rank>, val files: Array<File>) {
    White(ranks = Rank.entries.reversed().toTypedArray(), files = File.entries.toTypedArray()),
    Black(ranks = Rank.entries.toTypedArray(), files = File.entries.reversed().toTypedArray());

    companion object {
        fun fromSide(player: Side): BoardOrientation = if (player == Side.WHITE) White else Black
    }
}

@Immutable
data class BoardColors(
    val edge: Color,
    val light: Color,
    val dark: Color,
    val movePreviousFrom: Color,
    val movePreviousTo: Color,
    val moveAvailable: Color,
    val squareSelected: Color,
)

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
    val colors = Design.colors
    val boardColors = remember {
        BoardColors(
            edge = colors.boardEdge,
            light = colors.boardLight,
            dark = colors.boardDark,
            movePreviousFrom = colors.boardMovePreviousFrom,
            movePreviousTo = colors.boardMovePreviousTo,
            moveAvailable = colors.boardMoveAvailable,
            squareSelected = colors.boardSquareSelected,
        )
    }
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
                colors = boardColors,
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
            colors = boardColors,
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
    colors: BoardColors,
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
                boardWidthPx = coordinates.size.width
            }
    ) {
        ChessBoardContents(
            board = board,
            colors = colors,
            orientation = orientation,
            onClick = onClick,
            activeAnimatingPiece = activeAnimatingPiece,
            piecesAlpha = piecesAlpha,
        )

        if (activeAnimatingPiece != null && boardWidthPx > 0) {
            AnimatedPieceOverlay(
                animatingPiece = activeAnimatingPiece,
                from = board.lastMove!!.first,
                to = board.lastMove!!.second,
                orientation = orientation,
                boardWidthPx = boardWidthPx,
            )
        }
        overlay()
    }
}

@Composable
private fun ChessBoardContents(
    board: BoardViewData,
    colors: BoardColors,
    orientation: BoardOrientation,
    onClick: (Locus) -> Unit,
    activeAnimatingPiece: SidedPiece?,
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
                    val lastMoveFrom = board.lastMove?.first == loc
                    val lastMoveTo = board.lastMove?.second == loc
                    val piece = if (activeAnimatingPiece != null && (lastMoveFrom || lastMoveTo)) null else board.at(loc)
                    BoardSquare(
                        side = loc.side,
                        piece = piece,
                        isLastMoveFrom = lastMoveFrom,
                        isLastMoveTo = lastMoveTo,
                        isSelected = board.selection == loc || board.availableMoves.contains(loc),
                        colors = colors,
                        piecesAlpha = piecesAlpha,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onClick(loc) }
                            .testTag { ChessBoardTags.square(loc) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedPieceOverlay(
    animatingPiece: SidedPiece,
    from: Locus,
    to: Locus,
    orientation: BoardOrientation,
    boardWidthPx: Int,
) {
    val squareSizePx = boardWidthPx / 8f
    val density = LocalDensity.current

    // Map coordinates directly to a float-based Animatable vector space
    val fromOffset = from.toOffset(orientation, squareSizePx)
    val toOffset = to.toOffset(orientation, squareSizePx)

    val animatedOffset = remember(from, to) { Animatable(fromOffset, Offset.VectorConverter) }
    LaunchedEffect(animatingPiece) {
        animatedOffset.snapTo(fromOffset)
        animatedOffset.animateTo(
            targetValue = toOffset,
            animationSpec = tween(durationMillis = PIECE_MOVE_ANIMATION_DURATION_MS)
        )
    }

    Box(
        modifier = Modifier
            .size(with(density) { squareSizePx.toDp() })
            .offset {
                val offset = animatedOffset.value
                IntOffset(
                    x = offset.x.roundToInt(),
                    y = offset.y.roundToInt()
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        ChessPiece(
            piece = animatingPiece,
            alpha = 1f,
        )
    }
}

private fun BoardOrientation.fileIndex(file: File): Int = if (this == BoardOrientation.White) file.ordinal else 7 - file.ordinal
private fun BoardOrientation.rankIndex(rank: Rank): Int = if (this == BoardOrientation.White) 7 - rank.ordinal else rank.ordinal

private fun Locus.toOffset(orientation: BoardOrientation, squareSizePx: Float): Offset =
    Offset(orientation.fileIndex(file) * squareSizePx, orientation.rankIndex(rank) * squareSizePx)

@Preview(showBackground = true)
@Composable
private fun WhitePerspectivePreview() {
    ChessBoardPreviewRoot(UiSettings.default().copy(showBorders = false)) {
        ChessBoard(
            board = BoardViewData.default().select(Locus.e2, listOf(Locus.e3, Locus.e4)),
            orientation = BoardOrientation.White,
            onClick = { _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlackPerspectivePreview() {
    ChessBoardPreviewRoot(UiSettings.default().copy(showBorders = false)) {
        ChessBoard(
            board = BoardViewData.default().select(Locus.e2, listOf(Locus.e3, Locus.e4)),
            orientation = BoardOrientation.Black,
            onClick = { _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WhitePerspectiveBordersPreview() {
    ChessBoardPreviewRoot {
        ChessBoard(
            board = BoardViewData.default().select(Locus.e2, listOf(Locus.e3, Locus.e4)),
            orientation = BoardOrientation.White,
            onClick = { _ -> },
        )
    }
}
