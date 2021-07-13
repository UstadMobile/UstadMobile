package com.ustadmobile.port.android.impl

import android.content.Context
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.*
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.ContentImportManagerImplAndroid
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStateEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_MAIN_COROUTINE_CONTEXT
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerAndroidImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.NanoHttpdCall
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStateEndpointImpl
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.*
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherJvm
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploadManagerCommonJvm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.ustadmobile.core.db.UmAppDatabase_AddUriMapping
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.AppConfig.KEY_PBKDF2_ITERATIONS
import com.ustadmobile.core.impl.AppConfig.KEY_PBKDF2_KEYLENGTH
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_LOCAL_HTTP_PORT
import com.ustadmobile.core.io.ext.siteDataSubDir
import com.ustadmobile.core.networkmanager.*
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.port.android.network.downloadmanager.ContainerDownloadNotificationListener
import com.ustadmobile.port.android.util.ImageResizeAttachmentFilter
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import okhttp3.OkHttpClient

import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.lib.db.entities.ContainerImportJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Note: BaseUstadApp extends MultidexApplication on the multidex variant, but extends the
 * normal android.app.Application on non-multidex variants.
 */
open class UstadApp : BaseUstadApp(), DIAware {

    val diModule = DI.Module("UstadApp-Android") {
        import(commonJvmDiModule)

        bind<UstadMobileSystemImpl>() with singleton {
            UstadMobileSystemImpl.instance
        }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(instance(), applicationContext, di)
        }

        bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val contextIdentifier: String = sanitizeDbNameFromUrl(context.url)
            systemImpl.getOrGenerateNodeIdAndAuth(contextPrefix = contextIdentifier, applicationContext)
        }

        bind<UmAppDatabase>(tag = TAG_DB) with scoped(EndpointScope.Default).singleton {
            val dbName = sanitizeDbNameFromUrl(context.url)
            UmAppDatabase.getInstance(context = applicationContext, dbName = dbName,
                nodeIdAndAuth = instance()
            ).also {
                val networkManager: NetworkManagerBle = di.direct.instance()
                it.connectivityStatusDao.commitLiveConnectivityStatus(networkManager.connectivityStatus)
            }
        }

        bind<UmAppDatabase>(tag = TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            instance<UmAppDatabase>(tag = TAG_DB).asRepository(repositoryConfig(applicationContext,
                context.url, nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, instance(), instance()
            ) {
                attachmentsDir = File(applicationContext.filesDir.siteDataSubDir(this@singleton.context),
                    UstadMobileSystemCommon.SUBDIR_ATTACHMENTS_NAME).absolutePath
                useClientSyncManager = true
                attachmentFilters += ImageResizeAttachmentFilter("PersonPicture", 1280, 1280)
            }).also {
                (it as? DoorDatabaseRepository)?.setupWithNetworkManager(instance())
            }
        }

        bind<EmbeddedHTTPD>() with singleton {
            EmbeddedHTTPD(0, di).also {
                it.UmAppDatabase_AddUriMapping(false, "/:endpoint/UmAppDatabase", di)
                it.start()
                Napier.i("EmbeddedHTTPD started on port ${it.listeningPort}")
            }
        }

        bind<NetworkManagerBle>() with singleton {
            val coroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            NetworkManagerBle(applicationContext, di, coroutineDispatcher).also {
                it.onCreate()
            }
        }

        bind<BleGattServer>() with singleton {
            BleGattServer(applicationContext, di)
        }

        bind<ContainerMounter>() with singleton { instance<EmbeddedHTTPD>() }

        bind<ClazzLogCreatorManager>() with singleton { ClazzLogCreatorManagerAndroidImpl(applicationContext) }

        constant(TAG_DOWNLOAD_ENABLED) with true

        bind<ContainerDownloadManager>() with scoped(EndpointScope.Default).singleton {
            ContainerDownloadManagerImpl(endpoint = context, di = di).also {
                it.addContainerDownloadListener(ContainerDownloadNotificationListener(applicationContext, context))
            }
        }

        bind<DownloadPreparationRequester>() with scoped(EndpointScope.Default).singleton {
            DownloadPreparationRequesterAndroidImpl(applicationContext, context)
        }

        bind<DeletePreparationRequester>() with scoped(EndpointScope.Default).singleton {
            DeletePreparationRequesterAndroidImpl(applicationContext, context)
        }


        bind<ContainerDownloadRunner>() with factory { arg: DownloadJobItemRunnerDIArgs ->
            DownloadJobItemRunner(arg.downloadJobItem,
                    arg.endpoint.url, di = di)
        }

        bind<CoroutineDispatcher>(tag = TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

        bind<LocalAvailabilityManager>() with scoped(EndpointScope.Default).singleton {
            LocalAvailabilityManagerImpl(di, context)
        }

        bind<ContainerFetcher>() with singleton { ContainerFetcherJvm(di) }

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }

        bind<ContainerUploadManager>() with scoped(EndpointScope.Default).singleton {
            ContainerUploadManagerCommonJvm(di, context)
        }

        bind<ContentImportManager>() with scoped(EndpointScope.Default).singleton{
            ContentImportManagerImplAndroid(listOf(EpubTypePluginCommonJvm(),
                    XapiTypePluginCommonJvm(), VideoTypePluginAndroid(),
                    H5PTypePluginCommonJvm()), applicationContext,
                    context, di)
        }


        bind<Gson>() with singleton {
            val builder = GsonBuilder()
            builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
            builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
            builder.registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
            builder.create()
        }

        bind<GsonSerializer>() with singleton {
            GsonSerializer()
        }

        bind<XapiStatementEndpoint>() with scoped(EndpointScope.Default).singleton {
            XapiStatementEndpointImpl(endpoint = context, di = di)
        }
        bind<XapiStateEndpoint>() with scoped(EndpointScope.Default).singleton {
            XapiStateEndpointImpl(endpoint = context, di = di)
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

        bind<XmlSerializer>() with provider {
            instance<XmlPullParserFactory>().newSerializer()
        }

        bind<DestinationProvider>() with singleton {
            ViewNameToDestMap()
        }

        bind<Pbkdf2Params>() with singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val numIterations = systemImpl.getAppConfigInt(KEY_PBKDF2_ITERATIONS,
                UstadMobileConstants.PBKDF2_ITERATIONS, applicationContext)
            val keyLength = systemImpl.getAppConfigInt(KEY_PBKDF2_KEYLENGTH,
                UstadMobileConstants.PBKDF2_KEYLENGTH, applicationContext)

            Pbkdf2Params(numIterations, keyLength)
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

        registerContextTranslator { call: NanoHttpdCall -> Endpoint(call.urlParams["endpoint"] ?: "notfound")}

        onReady {
            instance<BleGattServer>()
            instance<NetworkManagerBle>()
            instance<EmbeddedHTTPD>()

            Picasso.setSingletonInstance(Picasso.Builder(applicationContext)
                    .downloader(OkHttp3Downloader(instance<OkHttpClient>()))
                    .build())
        }
    }

    override val di: DI by DI.lazy {
        import(diModule)
    }

    override fun onCreate() {
        super.onCreate()
        val systemImpl: UstadMobileSystemImpl = di.direct.instance()
        systemImpl.messageIdMap = MessageIDMap.ID_MAP
        Napier.base(DebugAntilog())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

}