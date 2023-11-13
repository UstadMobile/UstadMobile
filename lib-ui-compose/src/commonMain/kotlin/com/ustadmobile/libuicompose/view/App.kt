package com.ustadmobile.libuicompose.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.impl.appstate.AppUiState
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import dev.icerock.moko.resources.StringResource

data class TopNavigationItem(
    val destRoute: String,
    val icon: ImageVector,
    val label: StringResource,
)

val APP_TOP_LEVEL_NAV_ITEMS = listOf(
    TopNavigationItem(
        destRoute = ClazzListViewModel.DEST_NAME_HOME,
        icon = Icons.Outlined.School,
        label = MR.strings.courses,
    ),
    TopNavigationItem(
        destRoute = ContentEntryListViewModel.DEST_NAME_HOME,
        icon = Icons.Outlined.LocalLibrary,
        label = MR.strings.library,
    ),
    TopNavigationItem(
        destRoute = PersonListViewModel.DEST_NAME,
        icon = Icons.Outlined.Person,
        label = MR.strings.people,
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()

        val appUiState = remember {
            mutableStateOf(AppUiState())
        }

        val appUiStateVal by appUiState

        val canGoBack by navigator.canGoBack.collectAsState(false)

        MaterialTheme {
            Scaffold(
                topBar = {
                    //As per https://developer.android.com/jetpack/compose/components/app-bars#small
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        title = {
                            Text(appUiStateVal.title ?: stringResource(MR.strings.app_name))
                        },
                        navigationIcon = {
                            if(canGoBack) {
                                IconButton(
                                    modifier = Modifier.testTag("back_button"),
                                    onClick = {
                                        navigator.goBack()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowLeft,
                                        contentDescription = stringResource(MR.strings.back)
                                    )
                                }
                            }
                        },
                        actions = {
                            if(appUiStateVal.searchState.visible) {
                                OutlinedTextField(
                                    modifier = Modifier.testTag("search_box"),
                                    singleLine = true,
                                    trailingIcon = {
                                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                                    },
                                    value = appUiStateVal.searchState.searchText,
                                    placeholder = {
                                        Text(text = stringResource(MR.strings.search))
                                    },
                                    onValueChange = appUiStateVal.searchState.onSearchTextChanged,
                                )
                            }

                            if(appUiStateVal.actionBarButtonState.visible) {
                                Button(
                                    onClick = appUiStateVal.actionBarButtonState.onClick,
                                    enabled = appUiStateVal.actionBarButtonState.enabled,
                                    modifier = Modifier.testTag("action_bar_button"),
                                ) {
                                    Text(appUiStateVal.actionBarButtonState.text ?: "")
                                }
                            }else if(appUiStateVal.userAccountIconVisible) {
                                IconButton(
                                    onClick = {

                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = stringResource(MR.strings.account)
                                    )
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    //As per https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#navigationbar
                    var selectedTopLevelItem by remember { mutableIntStateOf(0) }
                    if(appUiStateVal.navigationVisible) {
                        NavigationBar {
                            APP_TOP_LEVEL_NAV_ITEMS.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(item.icon, contentDescription = null)
                                    },
                                    label = { Text(stringResource(item.label)) },
                                    selected = false,
                                    onClick = {
                                        selectedTopLevelItem = index
                                        navigator.navigate("/${item.destRoute}")
                                    }
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {

                },
            ) { innerPadding ->
                AppNavHost(
                    navigator = navigator,
                    onSetAppUiState = appUiState.component2(),
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}