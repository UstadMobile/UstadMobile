package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseAndroid
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.di.AndroidDomainDiModule
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderAndroid
import com.ustadmobile.core.networkmanager.ConnectionManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerAndroidImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.NanoHttpdCall
import com.ustadmobile.libuicompose.theme.AppTheme
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.compose.withDI
import com.ustadmobile.libuicompose.view.app.App
import com.ustadmobile.libuicompose.view.app.SizeClass
import moe.tlaster.precompose.PreComposeApp
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.provider
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.kodein.di.with
import java.io.File
import java.net.URI
import androidx.core.os.LocaleListCompat
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.nav.CommandFlowUstadNavController
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import com.ustadmobile.port.android.util.ext.getUstadDeepLink
import org.kodein.di.direct

class AppActivity: AppCompatActivity(), DIAware {

    private val appContextDi: DI by closestDI()

    //Used to execute navigation when a link is received via OnNewIntent
    private val commandFlowNavigator = CommandFlowUstadNavController()

    override val di by  DI.lazy {
        extend(appContextDi)

        import(commonDomainDiModule(EndpointScope.Default))
        import(AndroidDomainDiModule(applicationContext, EndpointScope.Default))

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

        registerContextTranslator { call: NanoHttpdCall -> Endpoint(call.urlParams["endpoint"] ?: "notfound") }

        onReady {
            instance<ConnectionManager>().start()
        }

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

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
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
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
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