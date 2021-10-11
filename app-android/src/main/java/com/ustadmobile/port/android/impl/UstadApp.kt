package com.ustadmobile.port.android.impl

import android.content.Context
import androidx.core.content.ContextCompat
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.ustadmobile.core.account.*
import com.ustadmobile.core.assignment.setupAssignmentSyncListener
import com.ustadmobile.core.catalog.contenttype.*
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStateEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobManagerAndroid
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.ContentPluginManagerImpl
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
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
import com.ustadmobile.core.db.UmAppDatabase_AddUriMapping
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.generated.locale.MessageID
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
import com.ustadmobile.core.torrent.ContainerTorrentDownloadJob
import com.ustadmobile.core.torrent.UstadCommunicationManager
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.torrent.UstadTorrentManagerImpl
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.util.getLocalIpAddress
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.*
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
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
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            val db = DatabaseBuilder.databaseBuilder(applicationContext, UmAppDatabase::class, dbName)
                .addMigrations(*UmAppDatabase.migrationList(nodeIdAndAuth.nodeId).toTypedArray())
                .addSyncCallback(nodeIdAndAuth, false)
                    .addCallback(ContentJobItemTriggersCallback())
                .build()
                .also {
                    val networkManager: NetworkManagerBle = di.direct.instance()
                    it.connectivityStatusDao.commitLiveConnectivityStatus(networkManager.connectivityStatus)
                }
            GlobalScope.launch {
                di.on(context).direct.instance<UstadTorrentManager>().start()
            }
            db
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
                it.setupAssignmentSyncListener(context, di)
            }
        }
        bind<File>(tag = DiTag.TAG_TORRENT_DIR) with scoped(EndpointScope.Default).singleton{
            val torrentDir = File(applicationContext.filesDir.siteDataSubDir(context), "torrent")
            torrentDir.mkdirs()
            torrentDir
        }

        bind<ContainerStorageManager> () with scoped(EndpointScope.Default).singleton{
            val systemImpl: UstadMobileSystemImpl = instance()
            val storageList = mutableListOf<ContainerStorageDir>()
            applicationContext.filesDir.listFiles()?.mapIndexed { index, it ->
                val siteDir = it.parentFile.siteDataSubDir(context)
                val storageDir = File(siteDir, UstadMobileSystemCommon.SUBDIR_CONTAINER_NAME)
                storageDir.takeIf { !it.exists() }?.mkdirs()
                val nameMessageId = if(index == 0) MessageID.phone_memory else MessageID.memory_card
                storageList.add(
                        ContainerStorageDir(storageDir.toURI().toString(),
                                systemImpl.getString(nameMessageId, applicationContext),
                        it.usableSpace, index == 0))
            }
            ContainerStorageManager(storageList.toList())
        }

        bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton{
            val containerStorage by di.instance<ContainerStorageManager>()
            val containerFolder = File(URI(containerStorage.storageList.first().dirUri))
            containerFolder.mkdirs()
            containerFolder
        }

        bind<ConnectivityLiveData>() with scoped(EndpointScope.Default).singleton {
            val db: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_DB)
            ConnectivityLiveData(db.connectivityStatusDao.statusLive())
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

        bind<ContentPluginManager>() with scoped(EndpointScope.Default).singleton {
            ContentPluginManagerImpl(listOf(
                    EpubTypePluginCommonJvm(applicationContext, context, di),
                    H5PTypePluginCommonJvm(applicationContext, context, di),
                    XapiTypePluginCommonJvm(applicationContext, context, di),
                    VideoTypePluginAndroid(applicationContext, context, di),
                    ContainerTorrentDownloadJob(context, di),
                    FolderIndexerPlugin(applicationContext, context, di)
                )
            )
        }
        bind<ContentJobManager>() with singleton {
            ContentJobManagerAndroid(applicationContext)
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

        bind<UstadTorrentManager>() with scoped(EndpointScope.Default).singleton {
            UstadTorrentManagerImpl(endpoint = context, di = di)
        }

        bind<UstadCommunicationManager>() with singleton {
            UstadCommunicationManager()
        }

        bind<Pbkdf2Params>() with singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val numIterations = systemImpl.getAppConfigInt(KEY_PBKDF2_ITERATIONS,
                UstadMobileConstants.PBKDF2_ITERATIONS, applicationContext)
            val keyLength = systemImpl.getAppConfigInt(KEY_PBKDF2_KEYLENGTH,
                UstadMobileConstants.PBKDF2_KEYLENGTH, applicationContext)

            Pbkdf2Params(numIterations, keyLength)
        }

        bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
            AuthManager(context, di)
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

        registerContextTranslator { call: NanoHttpdCall -> Endpoint(call.urlParams["endpoint"] ?: "notfound")}

        onReady {
            instance<BleGattServer>()
            instance<NetworkManagerBle>()
            instance<EmbeddedHTTPD>()

            val address: InetAddress? = getLocalIpAddress()
            instance<UstadCommunicationManager>().start(address)

            Picasso.setSingletonInstance(Picasso.Builder(applicationContext)
                    .downloader(OkHttp3Downloader(instance<OkHttpClient>()))
                    .build())
        }
    }

    lateinit var connectionManager: ConnectionManager

    override val di: DI by DI.lazy {
        import(diModule)
    }

    override fun onCreate() {
        super.onCreate()
        val systemImpl: UstadMobileSystemImpl = di.direct.instance()
        systemImpl.messageIdMap = MessageIDMap.ID_MAP
        Napier.base(DebugAntilog())
        connectionManager = ConnectionManager(this, di)
        connectionManager.startNetworkCallback()
    }

    override fun onTerminate() {
        super.onTerminate()
        connectionManager.stopNetworkCallback()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

}