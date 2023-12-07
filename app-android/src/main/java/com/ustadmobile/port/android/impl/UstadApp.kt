package com.ustadmobile.port.android.impl

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.toughra.ustadmobile.BuildConfig
import com.ustadmobile.core.account.*
import com.ustadmobile.core.contentformats.epub.EpubContentImporterCommonJvm
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.contentformats.h5p.H5PContentImportPlugin
import com.ustadmobile.core.contentformats.xapi.XapiZipContentImporter
import com.ustadmobile.core.contentjob.ContentImportersManager
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobManagerAndroid
import com.ustadmobile.core.db.*
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.*
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.core.db.ext.migrationList
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.AppConfig
import com.ustadmobile.core.impl.config.BundleAppConfig
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.LocaleSettingDelegateAndroid
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.METADATA_KEY_PRESET_LANG
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.PREFKEY_ACTIONED_PRESET
import com.ustadmobile.core.impl.nav.NavCommandExecutionTracker
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperAndroid
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

class UstadApp : Application(), DIAware, UstadLocaleChangeChannelProvider {

    private val Context.appMetaData: Bundle?
        get() = this.applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName, PackageManager.GET_META_DATA
        ).metaData


    override val localeChangeChannel: Channel<String> = Channel(capacity = 1)


    private val checkPresetLocaleCompletable = CompletableDeferred<Unit>()

    @OptIn(ExperimentalXmlUtilApi::class)
    override val di: DI by DI.lazy {
        import(CommonJvmDiModule)

        bind<AppConfig>() with singleton {
            BundleAppConfig(appMetaData)
        }

        bind<Settings>() with singleton {
            SharedPreferencesSettings(
                getSharedPreferences(UstadMobileSystemImpl.APP_PREFERENCES_NAME, Context.MODE_PRIVATE)
            )
        }

        bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
            val settings: Settings = instance()
            val contextIdentifier: String = sanitizeDbNameFromUrl(context.url)
            settings.getOrGenerateNodeIdAndAuth(contextIdentifier)
        }

        bind<SupportedLanguagesConfig>() with singleton {
            SupportedLanguagesConfig(
                systemLocales = LocaleListCompat.getAdjustedDefault().let { localeList ->
                    (0 .. localeList.size()).mapNotNull { localeList[it]?.language }
                },
                localeSettingDelegate = LocaleSettingDelegateAndroid(),
                availableLanguagesConfig = applicationContext.appMetaData?.getString(
                    METADATA_KEY_SUPPORTED_LANGS
                ) ?: SupportedLanguagesConfig.DEFAULT_SUPPORTED_LANGUAGES
            )
        }

        bind<ApiUrlConfig>() with singleton {
            ApiUrlConfig(
                presetApiUrl = applicationContext.appMetaData?.getString(METADATA_KEY_API_URL)
            )
        }

        bind<Json>() with singleton {
            Json {
                encodeDefaults = true
            }
        }

        bind<XML>() with singleton {
            XML {
                defaultPolicy {
                    unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                }
            }
        }

        bind<Gson>() with singleton {
            val builder = GsonBuilder()
            builder.create()
        }

        bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
            XmlPullParserFactory.newInstance()
        }

        bind<XmlSerializer>() with provider {
            instance<XmlPullParserFactory>().newSerializer()
        }

        bind<Pbkdf2Params>() with singleton {
            val numIterations = UstadMobileConstants.PBKDF2_ITERATIONS
            val keyLength = UstadMobileConstants.PBKDF2_KEYLENGTH

            Pbkdf2Params(numIterations, keyLength)
        }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(settings = instance(), di = di)
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
            val dbName = sanitizeDbNameFromUrl(context.url)
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            DatabaseBuilder.databaseBuilder(
                context = applicationContext,
                dbClass = UmAppDatabase::class,
                dbName = dbName,
                nodeId = nodeIdAndAuth.nodeId
            ).addSyncCallback(nodeIdAndAuth)
                .addCallback(ContentJobItemTriggersCallback())
                .addMigrations(*migrationList().toTypedArray())
                .build()

        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            db.asRepository(
                RepositoryConfig.repositoryConfig(
                    context = applicationContext,
                    endpoint = "${context.url}UmAppDatabase/",
                    nodeId = nodeIdAndAuth.nodeId,
                    auth = nodeIdAndAuth.auth,
                    httpClient = instance(),
                    okHttpClient = instance(),
                    json = instance()
                )
            )
        }

        bind<UriHelper>() with singleton {
            UriHelperAndroid(applicationContext)
        }

        bind<XhtmlFixer>() with singleton {
            XhtmlFixerJsoup(xml = instance())
        }

        bind<UstadCache>() with singleton {
            val httpCacheDir = File(applicationContext.filesDir, "httpfiles")
            val storagePath = Path(httpCacheDir.absolutePath)
            UstadCacheBuilder(applicationContext, storagePath).build()
        }

        bind<ContentImportersManager>() with scoped(EndpointScope.Default).singleton {
            val uriHelper: UriHelper = instance()
            val xml: XML = instance()
            val cache: UstadCache = instance()

            ContentImportersManager(
                listOf(
                    EpubContentImporterCommonJvm(
                        endpoint = context,
                        di = di,
                        cache = cache,
                        uriHelper = uriHelper,
                        xml = xml,
                        xhtmlFixer = instance()
                    ),
                    XapiZipContentImporter(
                        endpoint = context,
                        di = di,
                        cache = cache,
                        uriHelper = uriHelper
                    ),
                    H5PContentImportPlugin(
                        endpoint = context,
                        di = di,
                        cache = cache,
                        uriHelper = uriHelper,
                        json = instance(),
                    )
                )
            )
        }

        bind<ContentJobManager>() with singleton {
            ContentJobManagerAndroid(applicationContext)
        }

        bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
            XmlPullParserFactory.newInstance().also {
                it.isNamespaceAware = true
            }
        }

        bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
            AuthManager(context, di)
        }

        bind<NavCommandExecutionTracker>() with singleton {
            NavCommandExecutionTracker()
        }

        bind<GenderConfig>() with singleton {
            GenderConfig(
                appConfig = instance()
            )
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
    }


    private fun LocaleListCompat.getFirstLang() : String? {
        return try {
            this.get(0)?.toString()
        }catch(e: Exception) {
            null
        }
    }

    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())

        val metadataPresetLang = appMetaData?.getString(METADATA_KEY_PRESET_LANG)

        if(!metadataPresetLang.isNullOrEmpty()) {
            val settings: Settings = di.direct.instance()
            val presetActioned = settings.getStringOrNull(PREFKEY_ACTIONED_PRESET)
            if(presetActioned != true.toString()) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(metadataPresetLang))
                settings.putString(PREFKEY_ACTIONED_PRESET, true.toString())
            }

            checkPresetLocaleCompletable.complete(Unit)
        }


        /*
         * Horrible (but necessary) workaround to catch locale changes because events don't work,
         * see UstadLocaleChangeChannelProvider
         */
        var initialLang : String? = null
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            checkPresetLocaleCompletable.await()
            while(isActive) {
                delay(2000)
                val langNow = AppCompatDelegate.getApplicationLocales().getFirstLang()
                if(initialLang == null && langNow != null) {
                    Log.i("UstadApp", "found initial lang='$langNow'")
                    initialLang = langNow
                }else if(initialLang != null && langNow != initialLang && langNow != null) {
                    Log.i("UstadApp", "Detected locale change: init tags='$initialLang', tags now='$langNow'")
                    localeChangeChannel.trySend(langNow)
                    return@launch
                }
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if(BuildConfig.ACRA_HTTP_URI.isNotBlank()) {
            initAcra {
                reportFormat = StringFormat.JSON
                httpSender {
                    uri = BuildConfig.ACRA_HTTP_URI
                    basicAuthLogin = BuildConfig.ACRA_BASIC_LOGIN
                    basicAuthPassword = BuildConfig.ACRA_BASIC_PASS
                    httpMethod = HttpSender.Method.POST
                }
            }
        }
    }

    companion object {

        const val METADATA_KEY_SUPPORTED_LANGS = "com.ustadmobile.uilanguages"

        const val METADATA_KEY_API_URL = "com.ustadmobile.apiurl"
    }

}