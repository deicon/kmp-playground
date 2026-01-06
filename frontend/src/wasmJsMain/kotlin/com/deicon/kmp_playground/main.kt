package com.deicon.kmp_playground

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.deicon.kmp_playground.ui.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(title = "KMP Playground - Todo App", canvasElementId = "ComposeTarget") {
        App()
    }
}
