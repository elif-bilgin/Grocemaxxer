package com.grocemaxxer.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "grocemaxxer",
        state = WindowState(size = DpSize(430.dp, 860.dp)),
    ) {
        App()
    }
}
