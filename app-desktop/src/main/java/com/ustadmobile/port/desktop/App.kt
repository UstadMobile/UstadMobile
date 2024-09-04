package com.ustadmobile.port.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jcabi.manifests.Manifests
import com.ustadmobile.core.MR
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJvm
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.PREFKEY_LOCALE
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.PREFKEY_ACTIONED_PRESET
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.core.logging.LogbackAntiLog
import com.ustadmobile.libuicompose.theme.UstadAppTheme
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
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
import it.sauronsoftware.junique.AlreadyLockedException
import it.sauronsoftware.junique.JUnique
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.rememberNavigator
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.quartz.Scheduler
import java.awt.Desktop
import java.awt.Window
import java.io.File
import java.net.URI
import java.util.Locale
import java.util.Properties
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess
import com.ustadmobile.libuicompose.view.app.App as UstadPrecomposeApp

const val JUNIQUE_LOCK_ID = "com.ustadmobile.apprunlearningtree.lock"

/*
 * Note this is called by AppRun.kt to ensure that if the user has multiple versions (e.g. multiple
 * brands) each has its own id. See common on AppRun.kt
 *
 * Roughly as per https://github.com/JetBrains/compose-multiplatform-desktop-template#readme
 */
fun main() {
    val windowRef = AtomicReference<Window?>(null)
    try {
        JUnique.acquireLock(JUNIQUE_LOCK_ID) { message ->
            //The user tried to launch another instance. We will just bring the existing instance to front
            val window = windowRef.get()
            if(window != null) {
                Napier.d { "Received JUnique message: $message . Will attempt to move window to front." }
                window.toFront()
            }else {
                Napier.w("Received JUnique message $message, but window reference is null")
            }

            "OK"
        }
    }catch(e: AlreadyLockedException) {
        println("There is already another instance running: sending message")
        JUnique.sendMessage(JUNIQUE_LOCK_ID,"front")
        println("There is already another instance running: exiting")
        return
    }

    //Apply the language setting before startup
    val dataRoot = ustadAppDataDir()

    SetLanguageUseCaseJvm.init()

    var splashScreen: SplashScreen? = SplashScreen()

    //Set the logging directory to use the directory log within the data directory
    val logDir = File(ustadAppDataDir(), "log")
    logDir.takeIf { !it.exists() }?.mkdirs()
    System.setProperty("logs_dir", logDir.absolutePath)
    println("UstadMobile Desktop App" +
            "AppDataDir=${ustadAppDataDir()} \n" +
            "ResourcesDir=${ustadAppResourcesDir()} \n" +
            "Logging to ${logDir.absolutePath}"
    )

    Napier.base(LogbackAntiLog())

    val prefsPropertiesFiles = File(dataRoot, UstadMobileSystemImpl.PREFS_FILENAME)

    val schedulerRef = AtomicReference<Scheduler?>()

    try {
        val props = prefsPropertiesFiles.takeIf {
            it.exists()
        }?.inputStream()?.reader()?.use { inReader ->
            Properties().also { it.load(inReader) }
        } ?: Properties()

        val presetLocaleToAction = if(props.getProperty(PREFKEY_ACTIONED_PRESET) == null) {
            try {
                Manifests.read("com-ustadmobile-presetlocale")
            }catch(e: Throwable) {
                null
            }
        }else {
            null
        }

        if(!presetLocaleToAction.isNullOrBlank()) {
            props.setProperty(PREFKEY_LOCALE, presetLocaleToAction)
            props.setProperty(PREFKEY_ACTIONED_PRESET, "true")
            prefsPropertiesFiles.parentFile?.takeIf { !it.exists() }?.mkdirs()

            prefsPropertiesFiles.writer().use { propFileWriter ->
                props.store(propFileWriter, "")
            }
        }

        val langSetting: String? = props.getProperty(PREFKEY_LOCALE)

        if(!langSetting.isNullOrBlank() &&
            langSetting in SupportedLanguagesConfig.DEFAULT_SUPPORTED_LANGUAGES
        ) {
            Locale.setDefault(Locale(langSetting))
        }
    }catch(e: Throwable) {
        Napier.e("Exception handling locales on startup", e)
        System.err.println("failed to read language setting")
    }

    application(exitProcessOnExit = false) {
        Napier.base(DebugAntilog())

        //App icon setting as per
        // https://conveyor.hydraulic.dev/13.0/tutorial/tortoise/2-gradle/#setting-icons
        val appIcon = rememberAppResourcePainter("icon/icon-512.png")
        val topStartImg = rememberAppResourcePainter("topstart/top-start.png")

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
            val appVersion = remember {
                di.direct.instance<GetVersionUseCase>().invoke().versionString
            }
            val showPoweredBy = remember {
                di.direct.instance<GetShowPoweredByUseCase>().invoke()
            }


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
                schedulerRef.set(di.direct.instance())
            }

            CompositionLocalProvider(LocalKamelConfig provides desktopConfig) {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = appState.title ?: "",
                    icon = appIcon,
                    state = rememberWindowState(width = 1024.dp, height = 768.dp),
                ) {
                    LaunchedEffect(Unit) {
                        splashScreen?.close()
                        splashScreen = null
                    }
                    LaunchedEffect(window) {
                        windowRef.set(window)
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
                                        PermanentDrawerSheet(
                                            Modifier.width(240.dp).fillMaxHeight()
                                        ) {
                                            topStartImg?.also {
                                                Box(
                                                    modifier = Modifier.height(64.dp).fillMaxWidth(),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Image(
                                                        modifier = Modifier.defaultItemPadding(),
                                                        painter = it,
                                                        contentDescription = null
                                                    )
                                                }
                                            }

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
                                            Box(
                                                contentAlignment = Alignment.BottomCenter,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.Start
                                                ) {
                                                    Text(
                                                        text = "${stringResource(MR.strings.version)} $appVersion",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.defaultItemPadding(bottom = 4.dp),
                                                    )

                                                    if(showPoweredBy) {
                                                        Text(
                                                            modifier = Modifier.defaultItemPadding(top = 4.dp)
                                                                .pointerHoverIcon(PointerIcon.Hand)
                                                                .clickable {
                                                                    Desktop.getDesktop().browse(
                                                                        URI("https://www.ustadmobile.com/")
                                                                    )
                                                                },
                                                            text = stringResource(MR.strings.powered_by),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.Blue,
                                                            textDecoration = TextDecoration.Underline,
                                                        )
                                                    }
                                                }
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

    //Now shutdown and exit
    Napier.i("Shutdown: shutting down scheduler on app finished: scheduler=${schedulerRef.get()}")
    schedulerRef.get()?.shutdown()
    Napier.i("Shutdown: invoke exit process")
    exitProcess(0)
}
