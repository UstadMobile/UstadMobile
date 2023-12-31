package com.ustadmobile.port.android.impl

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
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
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.*
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCaseAndroid
import com.ustadmobile.core.domain.blob.savepicture.SavePictureUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCaseAndroid
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseAndroid
import com.ustadmobile.core.domain.upload.ChunkedUploadClientChunkGetterUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
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
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

class UstadApp : Application(), DIAware, ImageLoaderFactory{

    private val Context.appMetaData: Bundle?
        get() = this.applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName, PackageManager.GET_META_DATA
        ).metaData


    @OptIn(ExperimentalXmlUtilApi::class)
    override val di: DI by DI.lazy {
        bind<OkHttpClient>() with singleton {
            OkHttpClient.Builder()
                .dispatcher(
                    Dispatcher().also {
                        it.maxRequests = 30
                        it.maxRequestsPerHost = 10
                    }
                )
                .addInterceptor(
                    UstadCacheInterceptor(
                        cache = instance(),
                        cacheDir = File(applicationContext.filesDir, "httpfiles"),
                        logger = NapierLoggingAdapter(),
                    )
                )
                .build()
        }

        bind<HttpClient>() with singleton {
            HttpClient(OkHttp) {

                install(ContentNegotiation) {
                    json(json = instance())
                }
                install(HttpTimeout)

                val dispatcher = Dispatcher()
                dispatcher.maxRequests = 30
                dispatcher.maxRequestsPerHost = 10

                engine {
                    preconfigured = instance()
                }

            }
        }


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
            UstadCacheBuilder(applicationContext, storagePath, logger = NapierLoggingAdapter())
                .build()
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

        bind<ChunkedUploadClientUseCaseKtorImpl>() with singleton {
            ChunkedUploadClientUseCaseKtorImpl(
                httpClient = instance(),
                uriHelper = instance(),
            )
        }

        bind<ChunkedUploadClientLocalUriUseCase>() with singleton {
            instance<ChunkedUploadClientUseCaseKtorImpl>()
        }

        bind<ChunkedUploadClientChunkGetterUseCase>() with singleton {
            instance<ChunkedUploadClientUseCaseKtorImpl>()
        }

        bind<SaveLocalUrisAsBlobsUseCase>() with scoped(EndpointScope.Default).singleton {
            SaveLocalUrisAsBlobsUseCaseJvm(
                endpoint = context,
                cache = instance(),
                uriHelper = instance(),
                tmpDir = Path(applicationContext.cacheDir.absolutePath, "savelocaluriaslblobtmp"),
                fileSystem = SystemFileSystem
            )
        }

        bind<EnqueueBlobUploadClientUseCase>() with scoped(EndpointScope.Default).singleton {
            EnqueueBlobUploadClientUseCaseAndroid(
                appContext = applicationContext,
                endpoint = context,
                db = on(context).instance(tag = DoorTag.TAG_DB),
                cache = instance(),
            )
        }

        bind<BlobUploadClientUseCase>() with scoped(EndpointScope.Default).singleton {
            BlobUploadClientUseCaseJvm(
                chunkedUploadUseCase = on(context).instance(),
                httpClient = instance(),
                httpCache = instance(),
                db = on(context).instance(tag = DoorTag.TAG_DB),
                repo = on(context).instance(tag = DoorTag.TAG_REPO),
                endpoint = context,
            )
        }

        bind<EnqueueSavePictureUseCase>() with scoped(EndpointScope.Default).singleton {
            EnqueueSavePictureUseCaseAndroid(
                appContext = applicationContext,
                endpoint = context,
            )
        }

        bind<SavePictureUseCase>() with scoped(EndpointScope.Default).singleton {
            SavePictureUseCase(
                saveLocalUrisAsBlobUseCase = on(context).instance(),
                db = on(context).instance(tag = DoorTag.TAG_DB),
                repo = on(context).instance(tag = DoorTag.TAG_REPO),
                enqueueBlobUploadClientUseCase = on(context).instance(),
                compressImageUseCase = CompressImageUseCaseAndroid(applicationContext),
            )
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
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
        }
    }

    /**
     * COIL image loader singleton (connected to the OkHttpClient which uses UstadCache) as per
     *  https://coil-kt.github.io/coil/image_loaders/
     */
    override fun newImageLoader(): ImageLoader {
        val okHttpClient: OkHttpClient = di.direct.instance()
        return ImageLoader.Builder(applicationContext)
            .okHttpClient(
                okHttpClient = okHttpClient
            )
            .build()
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