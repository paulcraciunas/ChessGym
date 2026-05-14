package com.paulcraciunas.screens.common.design.components

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import com.paulcraciunas.screens.common.design.theme.Design

@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    Text(
        text = text.uppercase(),
        color = color ?: Design.colors.inkMuted,
        style = Design.typography.titleSmall,
        modifier = modifier,
    )
}

enum class EyebrowType { Soft, Muted, Danger }

/**
 * Uppercase, tracked, semi-bold label sat above titles and section heads.
 *
 *   Eyebrow("Profile")
 *   Eyebrow("Up Next", color = cg.accent)
 */
@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    type: EyebrowType = EyebrowType.Muted,
) {
    Text(
        text = text.uppercase(),
        color = when (type) {
            EyebrowType.Soft -> Design.colors.inkSoft
            EyebrowType.Muted -> Design.colors.inkMuted
            EyebrowType.Danger -> Design.colors.danger
        },
        style = when (type) {
            EyebrowType.Danger -> Design.textStyles.eyebrowLarge
            else -> Design.textStyles.eyebrow
        },
        modifier = modifier,
    )
}

@Composable
fun SectionHeaderTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        color = Design.colors.primary,
        style = Design.typography.labelMedium,
        modifier = modifier,
    )
}

/** Loads a string resource with escaped HTML (e.g. `&lt;b>bold&lt;/b>`) as [AnnotatedString]. */
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun annotatedTextResource(@StringRes id: Int): AnnotatedString {
    val rawString = LocalContext.current.getString(id)
    return AnnotatedString.fromHtml(htmlString = rawString)
}
