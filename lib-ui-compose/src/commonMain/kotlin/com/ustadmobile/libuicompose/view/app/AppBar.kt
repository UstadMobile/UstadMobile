package com.ustadmobile.libuicompose.view.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.appstate.AppBarColors
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.core.viewmodel.settings.SettingsViewModel
import com.ustadmobile.libuicompose.components.UstadActionButtonIcon
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.UstadTooltipBox
import com.ustadmobile.libuicompose.theme.appBarSelectionModeBackgroundColor
import com.ustadmobile.libuicompose.theme.appBarSelectionModeContentColor
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.Navigator
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

private val ROOT_LOCATIONS = UstadViewModel.ROOT_DESTINATIONS.map {
    "/$it"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UstadAppBar(
    compactHeader: Boolean,
    appUiState: AppUiState,
    navigator: Navigator,
    currentLocation: BackStackEntry?,
) {
    val title = appUiState.title ?: stringResource(MR.strings.app_name)
    val canGoBack by navigator.canGoBack.collectAsState(false)

    val di = localDI()
    val accountManager: UstadAccountManager = di.direct.instance()
    val currentSession by accountManager.currentUserSessionFlow
        .collectAsState(null)

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
    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        TopAppBar(
            colors = if(appUiState.appBarColors == AppBarColors.STANDARD) {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            }else {
                val contentColor = MaterialTheme.colorScheme.appBarSelectionModeContentColor
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.appBarSelectionModeBackgroundColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor,
                )
            },
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("app_title"),
                )
            },
            navigationIcon = {
                val leadingActionButton = appUiState.leadingActionButton
                when {
                    compactHeader && searchHasFocus -> {
                        //Space needed for search.
                    }
                    leadingActionButton != null -> {
                        UstadActionButtonIcon(leadingActionButton)
                    }
                    canGoBack -> {
                        IconButton(
                            modifier = Modifier.testTag("back_button"),
                            onClick = {
                                navigator.goBack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(MR.strings.back)
                            )
                        }
                    }
                }
            },
            actions = {
                appUiState.actionButtons.forEach {
                    UstadActionButtonIcon(it)
                }

                currentLocation?.path?.takeIf { path ->
                    val pathWithoutQuery = path.substringBefore("?")
                    !appUiState.hideSettingsIcon && ROOT_LOCATIONS.any { it.startsWith(pathWithoutQuery) }
                }?.also {
                    UstadTooltipBox(
                        tooltipText = stringResource(MR.strings.settings)
                    ) {
                        IconButton(
                            modifier = Modifier.testTag("settings_button"),
                            onClick = {
                                navigator.navigate("/${SettingsViewModel.DEST_NAME}")
                            }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(MR.strings.settings))
                        }
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
                                        modifier = Modifier.testTag("close_search_button"),
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
                            modifier = Modifier.testTag("expand_search_icon_button"),
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
                }else if(appUiState.overflowItems.isNotEmpty()) {
                    var popupMenuExpanded by remember {
                        mutableStateOf(false)
                    }

                    //As per
                    // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DropdownMenu(kotlin.Boolean,kotlin.Function0,androidx.compose.ui.Modifier,androidx.compose.ui.unit.DpOffset,androidx.compose.ui.window.PopupProperties,kotlin.Function1)
                    Box(
                        modifier = Modifier.wrapContentSize()
                    ) {
                        IconButton(
                            onClick = {
                                popupMenuExpanded = true
                            }
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(MR.strings.more_options))
                        }

                        DropdownMenu(
                            expanded = popupMenuExpanded,
                            onDismissRequest = {
                                popupMenuExpanded = false
                            }
                        ) {
                            appUiState.overflowItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.label) },
                                    onClick = {
                                        popupMenuExpanded = false
                                        item.onClick()
                                    }
                                )
                            }
                        }
                    }
                }else if(appUiState.userAccountIconVisible) {
                    IconButton(
                        modifier = Modifier.padding(8.dp).testTag("header_avatar"),
                        onClick = {
                            navigator.navigate("/${AccountListViewModel.DEST_NAME}")
                        }
                    ) {
                        UstadPersonAvatar(
                            personName = currentSession?.person?.fullName(),
                            pictureUri = currentSession?.personPicture?.personPictureThumbnailUri,
                        )
                    }
                }
            }
        )

        if(appUiState.loadingState.loadingState == LoadingUiState.State.INDETERMINATE) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).testTag("appbar_progress_bar")
            )
        }
    }

}
