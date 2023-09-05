package com.ustadmobile.port.desktop

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.libuicompose.helloworld.HelloWorld

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

    var appUiState = remember { mutableStateOf(AppUiState()) }


    Window(
        onCloseRequest = ::exitApplication,
        title = "Ustad Mobile",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {


        //Use appuistate to control the floating action button visibility, floating action button text, search visibility,  etc.


        MaterialTheme {
            val scaffoldState = rememberScaffoldState(
                drawerState = DrawerState(DrawerValue.Closed),
                snackbarHostState = SnackbarHostState()
            )
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        modifier = Modifier.height(80.dp),
                        navigationIcon = {
                            Row {
                                IconButton(onClick = { }) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowLeft,
                                        contentDescription = "Back"
                                    )
                                }
                                IconButton(onClick = { }) {
                                    Icon(
                                        modifier = Modifier.width(130.dp),
                                        imageVector = Icons.Filled.KeyboardArrowRight,
                                        contentDescription = "Back",
                                    )
                                }
                            }
                        },
                        title = {
                            TextField(
                                modifier = Modifier.testTag("username").fillMaxWidth()
                                    .padding(0.dp),
                                value = "username",
                                placeholder = { Text("Search...") },
                                label = {
                                    Text("Search...")
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                onValueChange = {},
                                enabled = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                        },
                        backgroundColor = MaterialTheme.colors.background
                    )
                },
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    FloatingActionButton(onClick = {}) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                },
                content = { HelloWorld() },
            )
        }
    }
}
