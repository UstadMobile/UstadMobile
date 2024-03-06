package com.ustadmobile.port.android.view

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseAndroid
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCaseAndroid
import com.ustadmobile.core.domain.contententry.move.MoveContentEntriesUseCase
import com.ustadmobile.core.domain.process.CloseProcessUseCase
import com.ustadmobile.core.domain.process.CloseProcessUseCaseAndroid
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.di.AndroidDomainDiModule
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderAndroid
import com.ustadmobile.core.impl.nav.CommandFlowUstadNavController
import com.ustadmobile.core.networkmanager.ConnectionManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerAndroidImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import com.ustadmobile.door.NanoHttpdCall
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.libuicompose.theme.UstadAppTheme
import com.ustadmobile.libuicompose.theme.md_theme_dark_primaryContainer
import com.ustadmobile.libuicompose.theme.md_theme_light_primaryContainer
import com.ustadmobile.libuicompose.view.app.App
import com.ustadmobile.libuicompose.view.app.SizeClass
import com.ustadmobile.port.android.util.ext.getUstadDeepLink
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.tlaster.precompose.PreComposeApp
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.bind
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.provider
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.kodein.di.with
import java.io.File
import java.net.URI


class AppActivity: AppCompatActivity(), DIAware {

    private val appContextDi: DI by closestDI()

    //Used to execute navigation when a link is received via OnNewIntent
    private val commandFlowNavigator = CommandFlowUstadNavController()

    //As per https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch/
    private var mCustomTabsClient: CustomTabsClient? = null

    private var mCustomTabsSession: CustomTabsSession? = null

    override val di by  DI.lazy {
        extend(appContextDi)

        import(commonDomainDiModule(EndpointScope.Default))
        import(AndroidDomainDiModule(applicationContext))

        bind<UstadMobileSystemImpl>() with singleton {
            /**
             * Context must be the activity, not the Application, because UstadMobileSystemImpl
             * is used to access strings. Per-app language settings are applied to the activity
             * context, not the application context.
             */
            UstadMobileSystemImpl(
                applicationContext = this@AppActivity,
                settings = instance(),
                langConfig = instance()
            )
        }

        bind<StringProvider>() with singleton {
            StringProviderAndroid(this@AppActivity)
        }

        //This must be on the Activity DI because it requires access to systemImpl
        bind<SetLanguageUseCase>() with provider {
            SetLanguageUseCaseAndroid(
                languagesConfig = instance()
            )
        }

        bind<ContainerStorageManager> () with scoped(EndpointScope.Default).singleton{
            ContainerStorageManager(applicationContext, context, di)
        }

        bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton{
            val containerStorage: ContainerStorageManager by di.on(context).instance()
            val uri = containerStorage.storageList.firstOrNull()?.dirUri ?: throw IllegalStateException("internal storage missing?")
            val containerFolder = File(URI(uri))
            containerFolder.mkdirs()
            containerFolder
        }

        bind<ClazzLogCreatorManager>() with singleton {
            ClazzLogCreatorManagerAndroidImpl(applicationContext)
        }

        constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with true

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }

        bind<ConnectionManager>() with singleton{
            ConnectionManager(applicationContext, di)
        }

        bind<LaunchXapiUseCase>() with scoped(EndpointScope.Default).provider {
            LaunchXapiUseCaseAndroid(
                androidContext = this@AppActivity,
                endpoint = context,
                resolveXapiLaunchHrefUseCase = instance(),
                lightToolbarColor = md_theme_light_primaryContainer.toArgb(),
                darkToolbarColor = md_theme_dark_primaryContainer.toArgb(),
                session = { mCustomTabsSession },
                getHtmlContentDisplayEngineUseCase = instance(),
                embeddedHttpServer = instance(),
            )
        }

        bind<MoveContentEntriesUseCase>() with scoped(EndpointScope.Default).provider {
            MoveContentEntriesUseCase(
                repo = instance(tag = DoorTag.TAG_REPO),
                systemImpl = instance()
            )
        }

        bind<CloseProcessUseCase>() with scoped(EndpointScope.Default).provider {
            CloseProcessUseCaseAndroid(this@AppActivity)
        }

        registerContextTranslator { call: NanoHttpdCall -> Endpoint(call.urlParams["endpoint"] ?: "notfound") }

        onReady {
            instance<ConnectionManager>().start()
        }

    }

    /*
     * Custom tabs setup as per https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch
     *
     * As per:
     * https://developer.chrome.com/docs/android/custom-tabs#when_should_i_use_custom_tabs
     * "Lifecycle management: Apps launching a Custom Tab won't be evicted by the system during the Tabs use - its importance is raised to the "foreground" level."
     *
     * The objective here isn't so much about tracking engagement, it is just to ensure that the
     * app itself is kept alive when the user is in the custom tab (which will rely on loading
     * content from the embedded server).
     */
    private val customTabCallback = object: CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            super.onNavigationEvent(navigationEvent, extras)
            Napier.d { "CustomTab: NavigationEvent: $navigationEvent" }
        }
    }

    private val mCustomTabsServiceConnection = object: CustomTabsServiceConnection() {

        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            Napier.d { "CustomTab: Service connected" }
            mCustomTabsClient = client
            Napier.d { "CustomTab: Warmup" }
            client.warmup(0)
            mCustomTabsSession = client.newSession(customTabCallback)
            Napier.d { "CustomTab: Session created" }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Napier.d { "CustomTabs: Service disconnected" }
            mCustomTabsClient = null
            mCustomTabsSession = null

            /*
             * As per
             * https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch
             * The custom tab connection might fail, so we need to reconnect if/when that happens.
             */
            lifecycleScope.launch {
                Napier.d { "CustomTab: disconnected, launching reconnection after 500ms" }
                delay(1_000)
                bindCustomTabsService()
            }
        }
    }

    private fun bindCustomTabsService() {
        if(mCustomTabsClient != null) {
            //do nothing
            return
        }

        val packageName = CustomTabsClient.getPackageName(this, null)
        CustomTabsClient.bindCustomTabsService(this, packageName, mCustomTabsServiceConnection)
    }


    val WindowWidthSizeClass.multiplatformSizeClass : SizeClass
        get() = when(this) {
            WindowWidthSizeClass.Compact -> SizeClass.COMPACT
            WindowWidthSizeClass.Medium -> SizeClass.MEDIUM
            WindowWidthSizeClass.Expanded -> SizeClass.EXPANDED
            else -> SizeClass.MEDIUM
        }

    override fun onLocalesChanged(locales: LocaleListCompat) {
        super.onLocalesChanged(locales)

        //App must be fully restart when locale changes - see SetLanguageUseCaseAndroid for details.
        ProcessPhoenix.triggerRebirth(this@AppActivity)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        /**
         * Trigger rebirth as required after a locale change. Horrible workaround. See
         * UstadLocaleChangeChannelProvider for an explanation of this.
         */
        enableEdgeToEdge()
        val openLink = intent.getUstadDeepLink()

        val initialRoute = "/" + RedirectViewModel.DEST_NAME.appendQueryArgs(
            buildMap {
                if(openLink != null)
                    put(UstadView.ARG_OPEN_LINK, openLink)
            }
        )

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            UstadAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().semantics {
                        testTagsAsResourceId = true
                    },
                    color = MaterialTheme.colorScheme.background,
                ) {
                    withDI(di) {
                        PreComposeApp {
                            App(
                                widthClass = windowSizeClass.widthSizeClass.multiplatformSizeClass,
                                navCommandFlow = commandFlowNavigator.commandFlow,
                                initialRoute = initialRoute,
                            )
                        }
                    }
                }
            }
        }

        //Official docs make no mention of disconnecting, does not seem to be required
        // https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch
        bindCustomTabsService()
    }

    override fun onDestroy() {
        Napier.d { "AppActivity#onDestroy" }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val uri = intent?.getUstadDeepLink()
        val argAccountName = intent?.getStringExtra(UstadViewModel.ARG_ACCOUNT_NAME)

        if(uri != null) {
            val apiUrlConfig: ApiUrlConfig = di.direct.instance()

            commandFlowNavigator.navigateToLink(
                link = uri,
                accountManager = di.direct.instance(),
                openExternalLinkUseCase = { _, _ -> Unit },
                forceAccountSelection = true,
                userCanSelectServer = apiUrlConfig.canSelectServer,
                accountName = argAccountName,
            )
        }
    }
}