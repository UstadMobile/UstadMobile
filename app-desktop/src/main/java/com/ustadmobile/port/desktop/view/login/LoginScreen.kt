package com.ustadmobile.port.desktop.view.login

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ustadmobile.libuicompose.view.login.LoginScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        val count = remember { mutableStateOf(0) }
        MaterialTheme {
            LoginScreen()
        }
    }
}