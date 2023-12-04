package com.ustadmobile.libuicompose.view.app

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.core.viewmodel.settings.SettingsViewModel
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.navigation.Navigator

private val ROOT_LOCATIONS = UstadViewModel.ROOT_DESTINATIONS.map {
    "/$it"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadAppBar(
    compactHeader: Boolean,
    appUiState: AppUiState,
    navigator: Navigator,
) {
    val title = appUiState.title ?: stringResource(MR.strings.app_name)
    val canGoBack by navigator.canGoBack.collectAsState(false)
    val currentLocation by navigator.currentEntry.collectAsState(null)

    var searchActive by remember {
        mutableStateOf(false)
    }

    var searchHasFocus by remember {
        mutableStateOf(false)
    }

    val focusRequester = remember { FocusRequester() }

    //Focus the search box when it appears after the user clicks the search icon
    LaunchedEffect(searchActive) {
        if(compactHeader && searchActive)
            focusRequester.requestFocus()
    }

    //As per https://developer.android.com/jetpack/compose/components/app-bars#small
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(title)
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
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(MR.strings.back)
                    )
                }
            }
        },
        actions = {
            currentLocation?.path?.takeIf { path -> ROOT_LOCATIONS.any { it.startsWith(path) } }?.also {
                IconButton(
                    modifier = Modifier.testTag("settings_button"),
                    onClick = {
                        navigator.navigate("/${SettingsViewModel.DEST_NAME}")
                    }
                ) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(MR.strings.settings))
                }
            }

            if(appUiState.searchState.visible) {
                if(!compactHeader || searchActive) {
                    OutlinedTextField(
                        modifier = Modifier.testTag("search_box")
                            .focusRequester(focusRequester)
                            .let {
                                if(compactHeader || searchHasFocus) {
                                    it.width(320.dp)
                                }else {
                                    it.width(192.dp)
                                }
                            }
                            .onFocusChanged {
                                searchHasFocus = it.hasFocus
                            },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if(searchActive) {
                                IconButton(
                                    onClick = {
                                        appUiState.searchState.onSearchTextChanged("")
                                        searchActive = false
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "")
                                }
                            }
                        },
                        value = appUiState.searchState.searchText,
                        placeholder = {
                            Text(text = stringResource(MR.strings.search))
                        },
                        onValueChange = appUiState.searchState.onSearchTextChanged,
                    )
                }else {
                    IconButton(
                        onClick = {
                            searchActive = true
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(MR.strings.search))
                    }
                }
            }

            if(appUiState.actionBarButtonState.visible) {
                Button(
                    onClick = appUiState.actionBarButtonState.onClick,
                    enabled = appUiState.actionBarButtonState.enabled,
                    modifier = Modifier.testTag("action_bar_button"),
                ) {
                    Text(appUiState.actionBarButtonState.text ?: "")
                }
            }else if(appUiState.userAccountIconVisible) {
                IconButton(
                    onClick = {
                        navigator.navigate("/${AccountListViewModel.DEST_NAME}")
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
}
