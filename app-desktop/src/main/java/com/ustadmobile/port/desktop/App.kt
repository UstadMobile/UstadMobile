package com.ustadmobile.port.desktop

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJvm
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.PREFKEY_LOCALE
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.libuicompose.theme.UstadAppTheme
import com.ustadmobile.libuicompose.view.app.APP_TOP_LEVEL_NAV_ITEMS
import com.ustadmobile.libuicompose.view.app.SizeClass
import dev.icerock.moko.resources.compose.stringResource
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.httpFetcher
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.ktor.client.HttpClient
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.rememberNavigator
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.File
import java.nio.file.Paths
import java.util.Locale
import java.util.Properties
import kotlin.io.path.exists
import kotlin.io.path.inputStream
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

fun main() {
    //Apply the language setting before startup
    val dataRoot = ustadAppDataDir()
    println("AppDataDir=${ustadAppDataDir()} ResourcesDir=${ustadAppResourcesDir()}")
    val splashScreen = SplashScreen()

    SetLanguageUseCaseJvm.init()

    val prefsProperties = File(dataRoot, UstadMobileSystemImpl.PREFS_FILENAME)
    if(prefsProperties.exists()) {
        try {
            prefsProperties.inputStream().reader().use { inReader ->
                val props = Properties().also { it.load(inReader) }
                val langSetting: String? = props.getProperty(PREFKEY_LOCALE)
                if(!langSetting.isNullOrBlank() &&
                    langSetting in SupportedLanguagesConfig.DEFAULT_SUPPORTED_LANGUAGES
                ) {
                    Locale.setDefault(Locale(langSetting))
                }
            }
        }catch(e: Exception) {
            System.err.println("failed to read language setting")
        }
    }

    application {
        Napier.base(DebugAntilog())

        //App icon setting as per
        // https://conveyor.hydraulic.dev/13.0/tutorial/tortoise/2-gradle/#setting-icons
        val appIcon = remember {
            ustadAppResourcesDir().let {
                Paths.get(it.absolutePath, "icon/icon-512.png")
            }.takeIf { it.exists() }
                ?.inputStream()
                ?.buffered()
                ?.use { BitmapPainter(loadImageBitmap(it)) }
        }

        var selectedItem by remember { mutableIntStateOf(0) }
        var appState by remember  {
            mutableStateOf(AppUiState(navigationVisible = false))
        }

        withDI(
            di = DI.from(listOf(
                DesktopDiModule,
                DesktopHttpModule,
                DesktopDomainDiModule,
                commonDomainDiModule(EndpointScope.Default),
            )),
        ) {
            val di = localDI()
            val desktopConfig = remember {
                KamelConfig {
                    takeFrom(KamelConfig.Default)
                    val httpClient: HttpClient = di.direct.instance()
                    httpFetcher(client = httpClient)
                }
            }

            LaunchedEffect(Unit) {
                di.direct.instance<EmbeddedHttpServer>().also {
                    it.start()
                    Napier.i("Embedded Server running on port ${it.listeningPort}")
                }
            }

            CompositionLocalProvider(LocalKamelConfig provides desktopConfig) {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = appState.title ?: "",
                    icon = appIcon,
                    state = rememberWindowState(width = 1024.dp, height = 768.dp),
                ) {
                    LaunchedEffect(Unit) {
                        splashScreen.close()
                    }
                    PreComposeApp {
                        val navigator = rememberNavigator()
                        val currentDestination by navigator.currentEntry.collectAsState(null)

                        /**
                         * Set the selected item. Relying on onClick misses when the user switches accounts
                         * and goes back to the start screen (courses).
                         */
                        LaunchedEffect(currentDestination?.path) {
                            val pathVal = currentDestination?.path ?: return@LaunchedEffect
                            val topLevelIndex = APP_TOP_LEVEL_NAV_ITEMS.indexOfFirst {
                                "/${it.destRoute}" == pathVal
                            }

                            if(topLevelIndex >= 0)
                                selectedItem = topLevelIndex
                        }

                        UstadAppTheme {
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
                                        onAppStateChanged = {
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

    }

}
