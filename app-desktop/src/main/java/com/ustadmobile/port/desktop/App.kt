package com.ustadmobile.port.desktop

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.libuicompose.view.app.APP_TOP_LEVEL_NAV_ITEMS
import com.ustadmobile.libuicompose.view.app.SizeClass
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.rememberNavigator
import org.kodein.di.DI
import org.kodein.di.compose.withDI
import com.ustadmobile.libuicompose.view.app.App as UstadPrecomposeApp

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
    var selectedItem by remember { mutableIntStateOf(0) }
    var appState by remember  {
        mutableStateOf(AppUiState(navigationVisible = false))
    }

    withDI(
        di = DI.from(listOf(
            DesktopDiModule,
            CommonJvmDiModule,
            commonDomainDiModule(EndpointScope.Default),
        )),
    ) {
        Window(
            onCloseRequest = ::exitApplication,
            title = appState.title ?: "",
            state = rememberWindowState(width = 1024.dp, height = 768.dp)
        ) {
            PreComposeApp {
                val navigator = rememberNavigator()
                MaterialTheme {
                    PermanentNavigationDrawer(
                        drawerContent = {
                            if(appState.navigationVisible) {
                                PermanentDrawerSheet(Modifier.width(240.dp)) {
                                    Spacer(Modifier.height(16.dp))
                                    APP_TOP_LEVEL_NAV_ITEMS.forEachIndexed { index, item ->
                                        NavigationDrawerItem(
                                            icon = { Icon(item.icon, contentDescription = null) },
                                            label = { Text(stringResource(item.label)) },
                                            selected = index == selectedItem,
                                            onClick = {
                                                selectedItem = index
                                                navigator.navigate(
                                                    route = "/${item.destRoute}",
                                                    options = NavOptions(popUpTo = PopUpTo.First(inclusive = true))
                                                )
                                            },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        },
                        content = {
                            UstadPrecomposeApp(
                                widthClass = SizeClass.EXPANDED,
                                navigator = navigator,
                                onAppStateChange = {
                                    appState = it
                                },
                                persistNavState = false,
                                useBottomBar = false,
                            )
                        }
                    )
                }
            }
        }
    }

}
