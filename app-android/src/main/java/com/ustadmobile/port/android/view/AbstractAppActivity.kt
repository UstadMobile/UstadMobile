package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.backup.AndroidUnzipFileUseCase
import com.ustadmobile.core.domain.backup.AndroidZipFileUseCase
import com.ustadmobile.core.domain.backup.UnzipFileUseCase
import com.ustadmobile.core.domain.backup.ZipFileUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUiUseCase
import com.ustadmobile.core.domain.contententry.move.MoveContentEntriesUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseAndroid
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsFromLocalUriUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsFromLocalUriUseCaseCommonJvm
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCaseImpl
import com.ustadmobile.core.domain.process.CloseProcessUseCase
import com.ustadmobile.core.domain.process.CloseProcessUseCaseAndroid
import com.ustadmobile.core.domain.share.SendAppFileUseCase
import com.ustadmobile.core.domain.share.SendAppFileUseCaseAndroid
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
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import com.ustadmobile.door.NanoHttpdCall
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.libuicompose.theme.UstadAppTheme
import com.ustadmobile.libuicompose.view.app.App
import com.ustadmobile.libuicompose.view.app.SizeClass
import com.ustadmobile.port.android.util.ext.getUstadDeepLink
import moe.tlaster.precompose.PreComposeApp
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.bind
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.kodein.di.with

abstract class AbstractAppActivity : AppCompatActivity(), DIAware {

    private val appContextDi: DI by closestDI()

    //Used to execute navigation when a link is received via OnNewIntent
    protected val commandFlowNavigator = CommandFlowUstadNavController()

    /**
     * The default initial route (Compose Navigation) to use. This can be overriden on activities
     * which are called for specific purposes (e.g. AuthenticatorActivity).
     */
    protected open val defaultInitialRoute: String? = null

    override val di by  DI.lazy {
        extend(appContextDi)

        import(commonDomainDiModule(EndpointScope.Default))
        import(AndroidDomainDiModule(applicationContext))


        bind<UnzipFileUseCase>() with singleton { AndroidUnzipFileUseCase(applicationContext) }
        bind<ZipFileUseCase>() with singleton { AndroidZipFileUseCase(applicationContext) }
        bind<SendAppFileUseCase>() with singleton { SendAppFileUseCaseAndroid(applicationContext) }

        bind<UstadMobileSystemImpl>() with singleton {
            /**
             * Context must be the activity, not the Application, because UstadMobileSystemImpl
             * is used to access strings. Per-app language settings are applied to the activity
             * context, not the application context.
             */
            UstadMobileSystemImpl(
                applicationContext = this@AbstractAppActivity,
                settings = instance(),
                langConfig = instance()
            )
        }

        bind<StringProvider>() with singleton {
            StringProviderAndroid(this@AbstractAppActivity)
        }

        //This must be on the Activity DI because it requires access to systemImpl
        bind<SetLanguageUseCase>() with provider {
            SetLanguageUseCaseAndroid(
                languagesConfig = instance()
            )
        }

        bind<ClazzLogCreatorManager>() with singleton {
            ClazzLogCreatorManagerAndroidImpl(applicationContext)
        }

        constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with true

        bind<ConnectionManager>() with singleton{
            ConnectionManager(applicationContext, di)
        }



        bind<MoveContentEntriesUseCase>() with scoped(EndpointScope.Default).provider {
            MoveContentEntriesUseCase(
                repo = instance(tag = DoorTag.TAG_REPO),
                systemImpl = instance()
            )
        }

        bind<CloseProcessUseCase>() with scoped(EndpointScope.Default).provider {
            CloseProcessUseCaseAndroid(this@AbstractAppActivity)
        }

        bind<OpenBlobUiUseCase>() with scoped(EndpointScope.Default).singleton {
            OpenBlobUiUseCase(
                openBlobUseCase = instance(),
                systemImpl = instance(),
            )
        }

        bind<BulkAddPersonsUseCase>() with scoped(EndpointScope.Default).provider {
            BulkAddPersonsUseCaseImpl(
                addNewPersonUseCase = instance(),
                validateEmailUseCase = instance(),
                validatePhoneNumUseCase = instance(),
                authManager = instance(),
                enrolUseCase = instance(),
                activeDb = instance(tag = DoorTag.TAG_DB),
                activeRepo = instance(tag = DoorTag.TAG_REPO),
            )
        }

        bind<BulkAddPersonsFromLocalUriUseCase>() with scoped(EndpointScope.Default).provider {
            BulkAddPersonsFromLocalUriUseCaseCommonJvm(
                bulkAddPersonsUseCase = instance(),
                uriHelper = instance(),
            )
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

        val initialRoute = defaultInitialRoute ?: ("/" + RedirectViewModel.DEST_NAME.appendQueryArgs(
            buildMap {
                if(openLink != null)
                    put(UstadView.ARG_OPEN_LINK, openLink)
            }
        ))

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