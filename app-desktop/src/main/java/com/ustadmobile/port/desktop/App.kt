package com.ustadmobile.port.desktop

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.setValue
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
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.libuicompose.helloworld.HelloWorld
import com.ustadmobile.libuicompose.view.siteenterlink.SiteEnterLinkScreen
import com.ustadmobile.libuicompose.view.siteenterlink.SiteEnterLinkScreenForViewModel
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
        Pair(stringResource(MR.strings.courses), Icons.Default.School),
        Pair(stringResource(MR.strings.library), Icons.Filled.LibraryBooks),
        Pair(stringResource(MR.strings.people), Icons.Filled.Person)
    )

    val appName = stringResource(MR.strings.app_name)
    val appUiState by remember {
        mutableStateOf(AppUiState(
        fabState = FabUiState(visible = true, icon = FabUiState.FabIcon.EDIT, text = "Edit"),
        searchState = AppBarSearchUiState(visible = true),
        title = appName,
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
                                onClick = { selectedItem = pair },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                },
                content = {
                    Scaffold(
                        topBar = { TopAppBar(appUiState) },
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
                        }
                    ) { _ ->
                        HelloWorld()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    appUiState: AppUiState,
){

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = { Text(appUiState.title ?: "") },
        navigationIcon = {
            Row {

                IconButton(onClick = {  }) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowLeft,
                        contentDescription = stringResource(MR.strings.back)
                    )
                }

                IconButton(onClick = {  }) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowRight,
                        contentDescription = stringResource(MR.strings.next)
                    )
                }
            }
        },

        actions = {

            if (appUiState.searchState.visible){
                OutlinedTextField(
                    modifier = Modifier
                        .testTag("searchBox"),
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    value = appUiState.searchState.searchText,
                    placeholder = { Text(text = stringResource(MR.strings.search)) },
                    onValueChange = appUiState.searchState.onSearchTextChanged,
                )
            }

            Spacer(Modifier.width(10.dp))

            if (appUiState.userAccountIconVisible){

                IconButton(
                    modifier = Modifier.testTag("account_button"),
                    onClick = {  }
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = stringResource(MR.strings.account)
                    )
                }

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