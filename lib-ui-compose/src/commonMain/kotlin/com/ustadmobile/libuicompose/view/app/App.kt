package com.ustadmobile.libuicompose.view.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavOptions
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
        destRoute = PersonListViewModel.DEST_NAME,
        icon = Icons.Outlined.Person,
        label = MR.strings.people,
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    widthClass: SizeClass = SizeClass.MEDIUM,
) {

    PreComposeApp {
        val navigator = rememberNavigator()

        val appUiState = remember {
            mutableStateOf(AppUiState())
        }

        val appUiStateVal by appUiState

        MaterialTheme {
            Scaffold(
                topBar = {
                    UstadAppBar(
                        compactHeader = (widthClass != SizeClass.EXPANDED),
                        appUiState = appUiStateVal,
                        navigator = navigator,
                    )
                },
                bottomBar = {
                    //As per https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#navigationbar
                    var selectedTopLevelItemIndex by remember { mutableIntStateOf(0) }
                    if(appUiStateVal.navigationVisible) {
                        NavigationBar {
                            APP_TOP_LEVEL_NAV_ITEMS.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(item.icon, contentDescription = null)
                                    },
                                    label = { Text(stringResource(item.label)) },
                                    selected = selectedTopLevelItemIndex == index,
                                    onClick = {
                                        selectedTopLevelItemIndex = index
                                        navigator.navigate(
                                            route  = "/${item.destRoute}",
                                            options = NavOptions(popUpTo = PopUpTo.First(inclusive = true))
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if(appUiStateVal.fabState.visible) {
                        ExtendedFloatingActionButton(
                            onClick = appUiStateVal.fabState.onClick,
                            text = {
                                Text(appUiStateVal.fabState.text ?: "")
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