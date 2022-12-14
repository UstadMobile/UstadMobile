package com.ustadmobile.port.android.impl

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.ustadmobile.core.account.*
import com.ustadmobile.core.assignment.ClazzAssignmentIncomingReplicationListener
import com.ustadmobile.core.catalog.contenttype.*
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStateEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobManagerAndroid
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.db.*
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.AppConfig.KEY_PBKDF2_ITERATIONS
import com.ustadmobile.core.impl.AppConfig.KEY_PBKDF2_KEYLENGTH
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_LOCAL_HTTP_PORT
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_MAIN_COROUTINE_CONTEXT
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.core.io.ext.siteDataSubDir
import com.ustadmobile.core.networkmanager.*
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.ClazzLogCreatorManagerAndroidImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.*
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.addIncomingReplicationListener
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.util.ImageResizeAttachmentFilter
import com.ustadmobile.core.contentformats.xapi.ContextDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementSerializer
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStateEndpointImpl
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
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
import java.util.concurrent.Executors
import com.ustadmobile.core.db.dao.commitLiveConnectivityStatus

open class UstadApp : Application(), DIAware {

    val diModule = DI.Module("UstadApp-Android") {
        import(commonJvmDiModule)

        bind<UstadMobileSystemImpl>() with singleton {
            UstadMobileSystemImpl()
        }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(instance(), applicationContext, di)
        }

        bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
            val systemImpl: UstadMobileSystemImpl = instance()
            val contextIdentifier: String = sanitizeDbNameFromUrl(context.url)
            systemImpl.getOrGenerateNodeIdAndAuth(contextPrefix = contextIdentifier, applicationContext)
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
            val dbName = sanitizeDbNameFromUrl(context.url)
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            val attachmentsDir = File(applicationContext.filesDir.siteDataSubDir(this@singleton.context),
                UstadMobileSystemCommon.SUBDIR_ATTACHMENTS_NAME)
            val attachmentFilters = listOf(ImageResizeAttachmentFilter("PersonPicture", 1280, 1280))
            DatabaseBuilder.databaseBuilder(applicationContext, UmAppDatabase::class, dbName,
                attachmentsDir, attachmentFilters)
                .addSyncCallback(nodeIdAndAuth)
                .addCallback(ContentJobItemTriggersCallback())
                .addMigrations(*migrationList().toTypedArray())
                .build()
                .also {
                    val networkManager: NetworkManagerBle = di.direct.instance()
                    it.connectivityStatusDao.commitLiveConnectivityStatus(networkManager.connectivityStatus)
                }
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
            db.asRepository(repositoryConfig(applicationContext,
                "${context.url}UmAppDatabase/", nodeIdAndAuth.nodeId, nodeIdAndAuth.auth,
                    instance(), instance()
            ) {
                useReplicationSubscription = true
                replicationSubscriptionInitListener = RepSubscriptionInitListener()
            }).also {
                (it as? DoorDatabaseRepository)?.setupWithNetworkManager(instance())
                it.addIncomingReplicationListener(
                    ClazzAssignmentIncomingReplicationListener(context, di))
            }
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

        bind<ContainerMounter>() with singleton { instance<EmbeddedHTTPD>() }

        bind<ClazzLogCreatorManager>() with singleton { ClazzLogCreatorManagerAndroidImpl(applicationContext) }

        constant(TAG_DOWNLOAD_ENABLED) with true

        bind<CoroutineDispatcher>(tag = TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }


        bind<EpubTypePluginCommonJvm>() with scoped(EndpointScope.Default).singleton{
            EpubTypePluginCommonJvm(applicationContext, context, di)
        }

        bind<XapiTypePluginCommonJvm>() with scoped(EndpointScope.Default).singleton{
            XapiTypePluginCommonJvm(applicationContext, context, di)
        }

        bind<H5PTypePluginCommonJvm>() with scoped(EndpointScope.Default).singleton{
            H5PTypePluginCommonJvm(applicationContext, context, di)
        }

        bind<VideoTypePluginAndroid>() with scoped(EndpointScope.Default).singleton{
            VideoTypePluginAndroid(applicationContext, context, di)
        }

        bind<PDFTypePlugin>() with scoped(EndpointScope.Default).singleton{
            PDFPluginAndroid(applicationContext, context, di)
        }

        bind<ContainerDownloadPlugin>() with scoped(EndpointScope.Default).singleton{
            ContainerDownloadPlugin(applicationContext, context, di)
        }

        bind<DeleteContentEntryPlugin>() with scoped(EndpointScope.Default).singleton{
            DeleteContentEntryPlugin(applicationContext, context, di)
        }

        bind<FolderIndexerPlugin>() with scoped(EndpointScope.Default).singleton{
            FolderIndexerPlugin(applicationContext, context, di)
        }

        bind<ContentPluginManager>() with scoped(EndpointScope.Default).singleton {
            ContentPluginManager(listOf(
                    di.on(context).direct.instance<EpubTypePluginCommonJvm>(),
                    di.on(context).direct.instance<XapiTypePluginCommonJvm>(),
                    di.on(context).direct.instance<H5PTypePluginCommonJvm>(),
                    di.on(context).direct.instance<VideoTypePluginAndroid>(),
                    di.on(context).direct.instance<PDFTypePlugin>(),
                    di.on(context).direct.instance<FolderIndexerPlugin>(),
                    di.on(context).direct.instance<ContainerDownloadPlugin>(),
                    di.on(context).direct.instance<DeleteContentEntryPlugin>(),
                    ContentEntryBranchDownloadPlugin(applicationContext, context, di)))
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

        bind<ConnectionManager>() with singleton{
            ConnectionManager(applicationContext, di)
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

        bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
            AuthManager(context, di)
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

        registerContextTranslator { call: NanoHttpdCall -> Endpoint(call.urlParams["endpoint"] ?: "notfound")}

        onReady {
            instance<NetworkManagerBle>()
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
        val systemImpl: UstadMobileSystemImpl = di.direct.instance()
        systemImpl.messageIdMap = MessageIDMap.ID_MAP
        Napier.base(DebugAntilog())
    }

    override fun onTerminate() {
        super.onTerminate()
        di.direct.instance<ConnectionManager>().stop()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

}