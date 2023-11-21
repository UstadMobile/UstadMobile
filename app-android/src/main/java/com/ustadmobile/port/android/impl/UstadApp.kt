package com.ustadmobile.port.android.impl

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.BuildConfig
import com.ustadmobile.core.account.*
import com.ustadmobile.core.contentformats.epub.EpubContentImporterCommonJvm
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.contentformats.h5p.H5PContentImportPlugin
import com.ustadmobile.core.contentformats.xapi.XapiZipContentImporter
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobManagerAndroid
import com.ustadmobile.core.contentjob.ContentImportersManager
import com.ustadmobile.core.db.*
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_LOCAL_HTTP_PORT
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_MAIN_COROUTINE_CONTEXT
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.networkmanager.*
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerAndroidImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.*
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.*
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.net.URI
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.di.AndroidDomainDiModule
import com.ustadmobile.core.impl.di.commonDomainDiModule
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderAndroid
import com.ustadmobile.core.impl.nav.NavCommandExecutionTracker
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperAndroid
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import kotlinx.io.files.Path
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

class UstadApp : Application(), DIAware {

    private val Context.appMetaData: Bundle?
        get() = this.applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName, PackageManager.GET_META_DATA
        ).metaData

    @OptIn(ExperimentalXmlUtilApi::class)
    val diModule = DI.Module("UstadApp-Android") {
        import(CommonJvmDiModule)
        import(commonDomainDiModule(EndpointScope.Default))
        import(AndroidDomainDiModule(applicationContext, EndpointScope.Default))

        bind<SupportedLanguagesConfig>() with singleton {
            applicationContext.appMetaData?.getString(METADATA_KEY_SUPPORTED_LANGS)?.let { langCodeList ->
                SupportedLanguagesConfig(langCodeList)
            } ?: SupportedLanguagesConfig()
        }

        bind<ApiUrlConfig>() with singleton {
            ApiUrlConfig(
                presetApiUrl = applicationContext.appMetaData?.getString(METADATA_KEY_API_URL)
            )
        }

        bind<UstadMobileSystemImpl>() with singleton {
            UstadMobileSystemImpl(applicationContext)
        }

        bind<StringProvider>() with singleton {
            StringProviderAndroid(applicationContext)
        }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(instance(), di)
        }

        bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val contextIdentifier: String = sanitizeDbNameFromUrl(context.url)
            systemImpl.getOrGenerateNodeIdAndAuth(contextPrefix = contextIdentifier, applicationContext)
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
            db.asRepository(repositoryConfig(
                context = applicationContext,
                endpoint = "${context.url}UmAppDatabase/",
                nodeId = nodeIdAndAuth.nodeId,
                auth = nodeIdAndAuth.auth,
                httpClient = instance(),
                okHttpClient = instance(),
                json = instance()
            ))
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

        constant(TAG_DOWNLOAD_ENABLED) with true

        bind<CoroutineDispatcher>(tag = TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }

        bind<UriHelper>() with singleton {
            UriHelperAndroid(applicationContext)
        }

        bind<XML>() with singleton {
            XML {
                defaultPolicy {
                    unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                }
            }
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

        bind<Gson>() with singleton {
            val builder = GsonBuilder()
            builder.create()
        }

        bind<Int>(tag = TAG_LOCAL_HTTP_PORT) with singleton {
            instance<EmbeddedHTTPD>().listeningPort
        }

        bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
            XmlPullParserFactory.newInstance().also {
                it.isNamespaceAware = true
            }
        }

        bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
            XmlPullParserFactory.newInstance()
        }

        bind<ConnectionManager>() with singleton{
            ConnectionManager(applicationContext, di)
        }

        bind<XmlSerializer>() with provider {
            instance<XmlPullParserFactory>().newSerializer()
        }


        bind<Pbkdf2Params>() with singleton {
            val numIterations = UstadMobileConstants.PBKDF2_ITERATIONS
            val keyLength = UstadMobileConstants.PBKDF2_KEYLENGTH

            Pbkdf2Params(numIterations, keyLength)
        }

        bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
            AuthManager(context, di)
        }

        bind<NavCommandExecutionTracker>() with singleton {
            NavCommandExecutionTracker()
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

        registerContextTranslator { call: NanoHttpdCall -> Endpoint(call.urlParams["endpoint"] ?: "notfound")}

        onReady {
            instance<EmbeddedHTTPD>()
            instance<ConnectionManager>().start()

            Picasso.setSingletonInstance(Picasso.Builder(applicationContext)
                    .downloader(OkHttp3Downloader(instance<OkHttpClient>()))
                    .build())
        }

        bind<Json>() with singleton {
            Json {
                encodeDefaults = true
            }
        }
    }

    override val di: DI by DI.lazy {
        import(diModule)
    }

    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
    }

    override fun onTerminate() {
        super.onTerminate()
        di.direct.instance<ConnectionManager>().stop()
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

        const val METADATA_KEY_SHARE_BASENAME = "com.ustadmobile.shareappbasename"

    }

}