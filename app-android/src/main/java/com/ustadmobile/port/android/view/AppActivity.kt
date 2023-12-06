package com.ustadmobile.port.android.view

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
import androidx.lifecycle.lifecycleScope
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase_AddUriMapping
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
import com.ustadmobile.port.android.impl.UstadLocaleChangeChannelProvider
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
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

class AppActivity: AppCompatActivity(), DIAware {


    private val appContextDi: DI by closestDI()

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

        bind<EmbeddedHTTPD>() with singleton {
            EmbeddedHTTPD(0, di).also {
                it.UmAppDatabase_AddUriMapping(false, "/:endpoint/UmAppDatabase", di)
                it.start()
                Napier.i("EmbeddedHTTPD started on port ${it.listeningPort}")
            }
        }

        bind<ClazzLogCreatorManager>() with singleton {
            ClazzLogCreatorManagerAndroidImpl(applicationContext)
        }

        constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with true

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }

        bind<Int>(tag = UstadMobileSystemCommon.TAG_LOCAL_HTTP_PORT) with singleton {
            instance<EmbeddedHTTPD>().listeningPort
        }


        bind<ConnectionManager>() with singleton{
            ConnectionManager(applicationContext, di)
        }

        registerContextTranslator { call: NanoHttpdCall -> Endpoint(call.urlParams["endpoint"] ?: "notfound") }

        onReady {
            instance<EmbeddedHTTPD>()
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

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Trigger rebirth as required after a locale change. Horrible workaround. See
         * UstadLocaleChangeChannelProvider for an explanation of this.
         */
        lifecycleScope.launch {
            (application as? UstadLocaleChangeChannelProvider)?.localeChangeChannel?.receive()
            ProcessPhoenix.triggerRebirth(this@AppActivity)
        }

        enableEdgeToEdge()

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
                                widthClass = windowSizeClass.widthSizeClass.multiplatformSizeClass
                            )
                        }
                    }
                }
            }
        }
    }
}