package com.paulcraciunas.screens.common.design.theme.walnut

import androidx.compose.ui.graphics.Color
import com.paulcraciunas.screens.common.design.theme.ChessGymColors

/** Walnut · Light. Editorial walnut + brass on cream paper. */
internal val WalnutLightColors = ChessGymColors(
    bg = Color(0xFFF4ECE0),
    bgTint = Color(0xFFECDFC9),
    surface = Color(0xFFFBF6EC),
    surfaceAlt = Color(0xFFF5ECD9),
    border = Color(0xFFDFD0B4),
    borderSoft = Color(0xFFEBDFC6),
    divider = Color(0x1A4A2C18),

    ink = Color(0xFF2A1E14),
    inkSoft = Color(0xFF5A4633),
    inkMuted = Color(0xFF8F7659),
    inkSubtle = Color(0xFFB6A487),
    onPrimary = Color(0xFFFBF6EC),

    primary = Color(0xFF5A2A1F),
    primaryDisabled = Color(0x995A2A1F),
    primarySoft = Color(0xFFF3E4D2),
    primarySoftDisabled = Color(0x99F3E4D2),
    primaryDeep = Color(0xFF3A1A13),
    accent = Color(0xFFB6873B),
    accentSoft = Color(0xFFF0E2C4),

    success = Color(0xFF4A6B3D),
    danger = Color(0xFF9A3220),

    boardLight = Color(0xFFEFDFC3),
    boardDark = Color(0xFFA8743F),
    pieceLight = Color(0xFFFAFAFA),
    pieceDark = Color(0xFF101A26),

    chipSolvedBg = Color(0xFFE9F3EB),
    chipSolvedBorder = Color(0xFFCFE5D2),
    chipSolvedInk = Color(0xFF2F6C43),
    chipAccentInk = Color(0xFF7A4F1A),

    medallionDepth = Color(0xFF2A1A10),

    isDark = false,
)

/** Walnut · Dark. Deep mahogany surfaces; brass becomes primary. */
internal val WalnutDarkColors = ChessGymColors(
    bg = Color(0xFF1A1209),
    bgTint = Color(0xFF241810),
    surface = Color(0xFF26190F),
    surfaceAlt = Color(0xFF1F1409),
    border = Color(0xFF3A2A1C),
    borderSoft = Color(0xFF2E1F12),
    divider = Color(0x14FFDCB4),

    ink = Color(0xFFF4E8D4),
    inkSoft = Color(0xFFD4C4AD),
    inkMuted = Color(0xFF9C8870),
    inkSubtle = Color(0xFF6E5E4A),
    onPrimary = Color(0xFF1A1209),

    primary = Color(0xFFD4A25C),
    primaryDisabled = Color(0x99D4A25C),
    primarySoft = Color(0x29D4A25C),
    primarySoftDisabled = Color(0x19D4A25C),
    primaryDeep = Color(0xFFB6873B),
    accent = Color(0xFFE8B95F),
    accentSoft = Color(0x2EE8B95F),

    success = Color(0xFF7FB085),
    danger = Color(0xFFD97560),

    boardLight = Color(0xFFC9A574),
    boardDark = Color(0xFF6B4424),
    pieceLight = Color(0xFFF4E8D4),
    pieceDark = Color(0xFF0C0805),

    chipSolvedBg = Color(0x2E7FB085),
    chipSolvedBorder = Color(0x5C7FB085),
    chipSolvedInk = Color(0xFF9BC7A0),
    chipAccentInk = Color(0xFFE8B95F),

    medallionDepth = Color(0xFF0E0904),

    isDark = true,
)
