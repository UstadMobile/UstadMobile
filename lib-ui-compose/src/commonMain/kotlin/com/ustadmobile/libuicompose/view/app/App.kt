package com.ustadmobile.libuicompose.view.app

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.rememberNavigator

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
        destRoute = ConversationListViewModel.DEST_NAME_HOME,
        icon = Icons.AutoMirrored.Outlined.Chat,
        label = MR.strings.messages,
    ),
    TopNavigationItem(
        destRoute = PersonListViewModel.DEST_NAME_HOME,
        icon = Icons.Outlined.Person,
        label = MR.strings.people,
    )
)

/**
 * @param onAppStateChanged - a change Listener that is used by the calling function, mostly the JVM
 *        window and activity. Used to update title, navbar visibility.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App(
    widthClass: SizeClass = SizeClass.MEDIUM,
    persistNavState: Boolean = false,
    useBottomBar: Boolean = true,
    navigator: Navigator = rememberNavigator(),
    onAppStateChanged: (AppUiState) -> Unit = { },
    navCommandFlow: Flow<NavCommand>? = null,
    initialRoute: String = "/${RedirectViewModel.DEST_NAME}",
) {
    val appUiState = remember {
        mutableStateOf(
            AppUiState(
                navigationVisible = false,
                hideAppBar = true,
            )
        )
    }

    val currentLocation by navigator.currentEntry.collectAsState(null)

    var appUiStateVal by appUiState
    LaunchedEffect(appUiStateVal) {
        onAppStateChanged(appUiStateVal)
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val onShowSnackBar: SnackBarDispatcher = remember {
        SnackBarDispatcher {  snack ->
            scope.launch {
                snackbarHostState.showSnackbar(snack.message, snack.action)
            }
        }
    }

    CompositionLocalProvider(LocalWidthClass provides widthClass) {
        Scaffold(
            topBar = {
                if(!appUiStateVal.hideAppBar) {
                    UstadAppBar(
                        compactHeader = (widthClass != SizeClass.EXPANDED),
                        appUiState = appUiStateVal,
                        navigator = navigator,
                        currentLocation = currentLocation,
                    )
                }
            },
            bottomBar = {
                //As per https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#navigationbar
                var selectedTopLevelItemIndex by remember { mutableIntStateOf(0) }
                if(useBottomBar) {
                    /**
                     * Set the selected item. Relying on onClick misses when the user switches accounts
                     * and goes back to the start screen (courses).
                     */
                    LaunchedEffect(currentLocation?.path) {
                        val pathVal = currentLocation?.path ?: return@LaunchedEffect
                        val topLevelIndex = APP_TOP_LEVEL_NAV_ITEMS.indexOfFirst {
                            "/${it.destRoute}" == pathVal
                        }

                        if(topLevelIndex >= 0)
                            selectedTopLevelItemIndex = topLevelIndex
                    }

                    if(appUiStateVal.navigationVisible && !appUiStateVal.hideBottomNavigation) {
                        NavigationBar {
                            APP_TOP_LEVEL_NAV_ITEMS.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(item.icon, contentDescription = null)
                                    },
                                    label = { Text(stringResource(item.label)) },
                                    selected = selectedTopLevelItemIndex == index,
                                    onClick = {
                                        navigator.navigate(
                                            route  = "/${item.destRoute}",
                                            options = NavOptions(popUpTo = PopUpTo.First(inclusive = true))
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if(appUiStateVal.fabState.visible) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.testTag("floating_action_button"),
                        onClick = appUiStateVal.fabState.onClick,
                        text = {
                            Text(
                                modifier = Modifier.testTag("floating_action_button_text"),
                                text = appUiStateVal.fabState.text ?: ""
                            )
                        },
                        icon = {
                            val imageVector = when(appUiStateVal.fabState.icon)  {
                                FabUiState.FabIcon.ADD -> Icons.Default.Add
                                FabUiState.FabIcon.EDIT -> Icons.Default.Edit
                                else -> null
                            }
                            if(imageVector != null) {
                                Icon(
                                    imageVector = imageVector,
                                    contentDescription = null,
                                )
                            }
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
        ) { innerPadding ->
            AppNavHost(
                navigator = navigator,
                onSetAppUiState = {
                    appUiStateVal = it
                },
                modifier = Modifier
                    .padding(innerPadding)
                    /*
                     * consumeWindowInsets is required so that subsequent use of imePadding doesn't result
                     * in extra space when the soft keyboard is open e.g. count the padding from the
                     * spacing against the padding required for the keyboard (otherwise both get added
                     * together).
                     */
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                persistNavState = persistNavState,
                onShowSnackBar = onShowSnackBar,
                navCommandFlow = navCommandFlow,
                initialRoute = initialRoute,
            )
        }
    }

}