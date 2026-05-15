package com.paulcraciunas.previews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paulcraciunas.screens.common.theme.ChessGymTheme

class DialogPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessGymTheme {
                DialogPreviewScreen()
            }
        }
    }
}
