package com.ustadmobile.port.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppBarSearchUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.libuicompose.helloworld.HelloWorld
import dev.icerock.moko.resources.compose.stringResource

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



val FAB_ICON_MAP = mapOf(
    FabUiState.FabIcon.ADD to Icons.Default.Add,
    FabUiState.FabIcon.EDIT to Icons.Default.Edit,
)


fun main() = application {


    val screens = listOf(
        Pair(stringResource(MR.strings.courses), Icons.Outlined.DateRange),
        Pair(stringResource(MR.strings.library), Icons.Filled.Favorite),
        Pair(stringResource(MR.strings.people), Icons.Default.Person)
    )

    val appUiState by remember { mutableStateOf(AppUiState(
        fabState = FabUiState(visible = true, icon = FabUiState.FabIcon.EDIT, text = "Edit"),
        searchState = AppBarSearchUiState(visible = true),
        userAccountIconVisible = false,
        actionBarButtonState = ActionBarButtonUiState(visible = true, text = "Save", enabled = true)
    )) }

    var selectedItem by remember { mutableStateOf(screens[0]) }

    Window(
        onCloseRequest = ::exitApplication,
        title = appUiState.title ?: "",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {


        MaterialTheme {

            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(Modifier.width(240.dp)) {
                        Spacer(Modifier.height(12.dp))
                        screens.forEach { pair ->
                            NavigationDrawerItem(
                                icon = { Icon(pair.second, contentDescription = null) },
                                label = { Text(pair.first) },
                                selected = pair == selectedItem,
                                onClick = {
                                    selectedItem = pair
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                },
                content = {
                    Scaffold(
                        topBar = { TopAppBar(appUiState) },
                        floatingActionButtonPosition = FabPosition.End,
                        floatingActionButton = {
                            if (appUiState.fabState.visible){
                                ExtendedFloatingActionButton(
                                    text = { appUiState.fabState.text?.let { Text(text = it) } },
                                    onClick = appUiState.fabState.onClick,
                                    icon = {
                                        FAB_ICON_MAP[appUiState.fabState.icon]?.let {
                                            Icon(
                                                imageVector = it,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                )
                            }
                        },
                    ) { contentPadding ->
                        HelloWorld()
                    }
                }
            )
        }
    }
}

@Composable
private fun TopAppBar(
    appUiState: AppUiState,
){

    TopAppBar(
        title = { Text(appUiState.title ?: "") },
        navigationIcon = {
            IconButton(
                onClick = {},
                contentDescription = stringResource(MR.strings.back),
                icon = Icons.Outlined.KeyboardArrowLeft
            )

            IconButton(
                onClick = {},
                contentDescription = stringResource(MR.strings.next),
                icon = Icons.Outlined.KeyboardArrowRight
            )
        },

        actions = {

            if (appUiState.searchState.visible){
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1.0f)
                        .testTag("searchBox"),
                    value = appUiState.searchState.searchText,
                    placeholder = { Text(text = stringResource(MR.strings.search)) },
                    onValueChange = appUiState.searchState.onSearchTextChanged,
                )
            } else {
                Box(modifier = Modifier.weight(1.0f))
            }

            if (appUiState.userAccountIconVisible){
                IconButton(
                    onClick = {  },
                    contentDescription = stringResource(MR.strings.account),
                    icon = Icons.Filled.AccountCircle)
            } else if (appUiState.actionBarButtonState.visible){
                Button(
                    onClick = appUiState.actionBarButtonState.onClick,
                    enabled = appUiState.actionBarButtonState.enabled,
                    modifier = Modifier.testTag("action_button"),
                ) {
                    Text(appUiState.actionBarButtonState.text ?: "")
                }
            }

        }
    )
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: ImageVector
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}