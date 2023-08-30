package com.ustadmobile.port.desktop

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ustadmobile.libuicompose.helloworld.HelloWorld
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource
import java.util.Locale

//Roughly as per https://github.com/JetBrains/compose-multiplatform-desktop-template#readme
/*
 * Clicking on the run button in the IDE directly **WILL NOT WORK** - it will not find the resource
 * bundles required (probably due to the joys of Modular Java).
 *
 * Use ./gradlew app-desktop:run to run it. To debug, run the Gradle app-desktop:run task in debug
 * mode in the IDE (this can be done by selecting the Gradle task from the Gradle pane on the right
 * of Android Studio - select app-desktop -> tasks -> compose desktop -> run, then right click on run
 * and select debug.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(MR.strings.app_name),
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        val count = remember { mutableStateOf(0) }
        MaterialTheme {
            HelloWorld()
        }
    }
}
