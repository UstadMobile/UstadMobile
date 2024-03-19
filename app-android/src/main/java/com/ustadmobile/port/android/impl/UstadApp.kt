package com.ustadmobile.port.android.impl

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
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
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentformats.epub.EpubContentImporterCommonJvm
import com.ustadmobile.core.contentformats.h5p.H5PContentImporter
import com.ustadmobile.core.contentformats.pdf.PdfContentImporterAndroid
import com.ustadmobile.core.contentformats.video.VideoContentImporterCommonJvm
import com.ustadmobile.core.contentformats.xapi.XapiZipContentImporter
import com.ustadmobile.core.db.*
import com.ustadmobile.core.db.ext.MIGRATION_144_145_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_148_149_CLIENT_WITH_OFFLINE_ITEMS
import com.ustadmobile.core.db.ext.MIGRATION_155_156_CLIENT
import com.ustadmobile.core.db.ext.MIGRATION_161_162_CLIENT
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.*
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.account.SetPasswordUseCase
import com.ustadmobile.core.domain.account.SetPasswordUseCaseCommonJvm
import com.ustadmobile.core.domain.blob.download.BlobDownloadClientUseCase
import com.ustadmobile.core.domain.blob.download.BlobDownloadClientUseCaseCommonJvm
import com.ustadmobile.core.domain.blob.download.CancelDownloadUseCase
import com.ustadmobile.core.domain.blob.download.CancelDownloadUseCaseAndroid
import com.ustadmobile.core.domain.blob.download.ContentManifestDownloadUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueBlobDownloadClientUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueBlobDownloadClientUseCaseAndroid
import com.ustadmobile.core.domain.blob.download.EnqueueContentManifestDownloadJobUseCaseAndroid
import com.ustadmobile.core.domain.blob.download.MakeContentEntryAvailableOfflineUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCaseAndroid
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.saveandupload.SaveAndUploadLocalUrisUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCaseAndroid
import com.ustadmobile.core.domain.blob.savepicture.SavePictureUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.CancelBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.CancelBlobUploadClientUseCaseAndroid
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCaseAndroid
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.domain.cachelock.AddOfflineItemInactiveTriggersCallback
import com.ustadmobile.core.domain.cachelock.UpdateCacheLockJoinUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCaseCommonJvm
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCase
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCaseAndroid
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseAndroid
import com.ustadmobile.core.domain.contententry.delete.DeleteContentEntryParentChildJoinUseCase
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueImportContentEntryUseCaseAndroid
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueImportContentEntryUseCaseRemote
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.getversion.GetVersionUseCaseAndroid
import com.ustadmobile.core.domain.htmlcontentdisplayengine.GetHtmlContentDisplayEngineOptionsUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.GetHtmlContentDisplayEngineUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.HTML_CONTENT_OPTIONS_ANDROID
import com.ustadmobile.core.domain.htmlcontentdisplayengine.SetHtmlContentDisplayEngineUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.ResolveXapiLaunchHrefUseCase
import com.ustadmobile.core.domain.deleteditem.DeletePermanentlyUseCase
import com.ustadmobile.core.domain.deleteditem.RestoreDeletedItemUseCase
import com.ustadmobile.core.domain.getdeveloperinfo.GetDeveloperInfoUseCase
import com.ustadmobile.core.domain.getdeveloperinfo.GetDeveloperInfoUseCaseAndroid
import com.ustadmobile.core.domain.share.ShareTextUseCase
import com.ustadmobile.core.domain.share.ShareTextUseCaseAndroid
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseAndroid
import com.ustadmobile.core.domain.upload.ChunkedUploadClientChunkGetterUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCase
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCaseAndroid
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
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
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.APPCONFIG_KEY_PRESET_LANG
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.PREFKEY_ACTIONED_PRESET
import com.ustadmobile.core.impl.nav.NavCommandExecutionTracker
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperAndroid
import com.ustadmobile.core.util.ext.appMetaData
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.util.ext.requireFileSeparatorSuffix
import com.ustadmobile.core.util.ext.toNullIfBlank
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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


    data class DbAndObservers(
        val db: UmAppDatabase,
        val updateCacheLockJoinUseCase: UpdateCacheLockJoinUseCase,
    )

    private val Context.httpPersistentFilesDir: File
        get() = File(filesDir, "httpfiles")


    @OptIn(ExperimentalXmlUtilApi::class)
    override val di: DI by DI.lazy {
        bind<OkHttpClient>() with singleton {
            val rootTmpDir: File = instance(tag = DiTag.TAG_TMP_DIR)

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
                        tmpDir = File(rootTmpDir, "okhttp-tmp"),
                        logger = NapierLoggingAdapter(),
                        json = instance(),
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

        bind<File>(tag = DiTag.TAG_TMP_DIR) with singleton {
            File(applicationContext.filesDir, "tmp")
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
                )?.toNullIfBlank() ?: SupportedLanguagesConfig.DEFAULT_SUPPORTED_LANGUAGES
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
                ignoreUnknownKeys = true
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

        bind<DbAndObservers>() with scoped(EndpointScope.Default).singleton {
            val dbName = sanitizeDbNameFromUrl(context.url)
            val nodeIdAndAuth: NodeIdAndAuth = instance()
            val db = DatabaseBuilder.databaseBuilder(
                context = applicationContext,
                dbClass = UmAppDatabase::class,
                dbName = dbName,
                nodeId = nodeIdAndAuth.nodeId
            ).addSyncCallback(nodeIdAndAuth)
                .addCallback(AddOfflineItemInactiveTriggersCallback())
                .addMigrations(*migrationList().toTypedArray())
                .addMigrations(MIGRATION_144_145_CLIENT)
                .addMigrations(MIGRATION_148_149_CLIENT_WITH_OFFLINE_ITEMS)
                .addMigrations(MIGRATION_155_156_CLIENT)
                .addMigrations(MIGRATION_161_162_CLIENT)
                .build()

            val cache: UstadCache = instance()

            DbAndObservers(
                db = db,
                updateCacheLockJoinUseCase = UpdateCacheLockJoinUseCase(
                    db = db,
                    cache = cache,
                )
            )
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
            instance<DbAndObservers>().db
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
            val httpCacheDir =  applicationContext.httpPersistentFilesDir
            val storagePath = Path(httpCacheDir.absolutePath)
            UstadCacheBuilder(
                appContext = applicationContext,
                storagePath = storagePath,
                logger = NapierLoggingAdapter(),
                sizeLimit = { 100_000_000L },
            ).build()
        }

        bind<ContentImportersManager>() with scoped(EndpointScope.Default).singleton {
            val cache: UstadCache = instance()
            val uriHelper: UriHelper = instance()
            val xml: XML = instance()
            val xhtmlFixer: XhtmlFixer = instance()
            val db: UmAppDatabase = instance(tag = DoorTag.TAG_DB)
            val saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase = instance()
            val tmpRoot: File = instance(tag = DiTag.TAG_TMP_DIR)
            val contentImportTmpPath = Path(tmpRoot.absolutePath, "contentimport")
            val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase= instance()

            ContentImportersManager(
                buildList {
                    add(
                        EpubContentImporterCommonJvm(
                            endpoint = context,
                            cache = cache,
                            db = db,
                            uriHelper = uriHelper,
                            xml = xml,
                            xhtmlFixer = xhtmlFixer,
                            tmpPath = contentImportTmpPath,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            json = instance(),
                            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                        )
                    )
                    add(
                        XapiZipContentImporter(
                            endpoint = context,
                            db = db,
                            cache = cache,
                            uriHelper = uriHelper,
                            json = instance(),
                            tmpPath = contentImportTmpPath,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                        )
                    )

                    add(
                        H5PContentImporter(
                            endpoint = context,
                            db = db,
                            cache = cache,
                            uriHelper = uriHelper,
                            tmpPath = contentImportTmpPath,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            json = instance(),
                            h5pInStream = {
                                applicationContext.assets.open("h5p/h5p-standalone-3.6.0.zip",
                                    AssetManager.ACCESS_STREAMING)
                            }
                        ),
                    )

                    add(
                        VideoContentImporterCommonJvm(
                            endpoint = context,
                            validateVideoFileUseCase = instance(),
                            uriHelper = uriHelper,
                            cache = cache,
                            tmpPath = contentImportTmpPath,
                            db = db,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            json = instance(),
                            getStoragePathForUrlUseCase  = getStoragePathForUrlUseCase,
                        )
                    )

                    add(
                        PdfContentImporterAndroid(
                            endpoint = context,
                            cache = cache,
                            uriHelper = uriHelper,
                            db = db,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                            json = instance(),
                            appContext = applicationContext,
                            tmpDir = File(contentImportTmpPath.toString()),
                        )
                    )
                }
            )
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



        bind<SaveLocalUrisAsBlobsUseCase>() with scoped(EndpointScope.Default).singleton {
            val rootTmpDir: File = instance(tag = DiTag.TAG_TMP_DIR)
            SaveLocalUrisAsBlobsUseCaseJvm(
                endpoint = context,
                cache = instance(),
                uriHelper = instance(),
                tmpDir = Path(rootTmpDir.absolutePath, "savelocaluriaslblobtmp"),
                fileSystem = SystemFileSystem,
                deleteUrisUseCase = instance(),
            )
        }

        bind<SaveLocalUriAsBlobAndManifestUseCase>() with scoped(EndpointScope.Default).singleton {
            SaveLocalUriAsBlobAndManifestUseCaseJvm(
                saveLocalUrisAsBlobsUseCase = instance(),
                mimeTypeHelper = FileMimeTypeHelperImpl(),
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
                json = instance(),
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

        bind<IsTempFileCheckerUseCase>() with singleton {
            IsTempFileCheckerUseCaseAndroid(
                tmpDir = instance(tag = DiTag.TAG_TMP_DIR)
            )
        }

        bind<DeleteUrisUseCase>() with singleton {
            DeleteUrisUseCaseCommonJvm(
                isTempFileCheckerUseCase = instance()
            )
        }

        bind<SavePictureUseCase>() with scoped(EndpointScope.Default).singleton {
            SavePictureUseCase(
                saveLocalUrisAsBlobUseCase = on(context).instance(),
                db = on(context).instance(tag = DoorTag.TAG_DB),
                repo = on(context).instance(tag = DoorTag.TAG_REPO),
                enqueueBlobUploadClientUseCase = on(context).instance(),
                compressImageUseCase = CompressImageUseCaseAndroid(applicationContext),
                deleteUrisUseCase = instance(),
            )
        }

        bind<UpdateFailedTransferJobUseCase>() with scoped(EndpointScope.Default).provider {
            UpdateFailedTransferJobUseCase(
                db = instance(tag = DoorTag.TAG_DB)
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

        bind<ContentEntryGetMetaDataFromUriUseCase>() with scoped(EndpointScope.Default).provider {
            ContentEntryGetMetaDataFromUriUseCaseCommonJvm(
                importersManager = instance()
            )
        }

        bind<EnqueueContentEntryImportUseCase>() with scoped(EndpointScope.Default).provider {
            EnqueueImportContentEntryUseCaseAndroid(
                db = instance(tag = DoorTag.TAG_DB),
                appContext = applicationContext,
                endpoint = context,
                enqueueRemoteImport = EnqueueImportContentEntryUseCaseRemote(
                    endpoint = context,
                    httpClient = instance(),
                    json = instance(),
                )
            )
        }

        bind<ImportContentEntryUseCase>() with scoped(EndpointScope.Default).provider {
            ImportContentEntryUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                importersManager = instance(),
                enqueueBlobUploadClientUseCase = instance(),
                createRetentionLocksForManifestUseCase = instance(),
                httpClient = instance(),
                json = instance(),
            )
        }

        bind<CreateRetentionLocksForManifestUseCase>() with scoped(EndpointScope.Default).singleton {
            CreateRetentionLocksForManifestUseCaseCommonJvm(
                cache = instance()
            )
        }

        bind<BlobDownloadClientUseCase>() with scoped(EndpointScope.Default).singleton {
            BlobDownloadClientUseCaseCommonJvm(
                okHttpClient = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance(tag = DoorTag.TAG_REPO),
                httpCache = instance(),
            )
        }

        bind<EnqueueBlobDownloadClientUseCase>() with scoped(EndpointScope.Default).singleton {
            EnqueueBlobDownloadClientUseCaseAndroid(
                appContext = applicationContext,
                endpoint = context,
                db = instance(tag = DoorTag.TAG_DB)
            )
        }

        bind<ContentManifestDownloadUseCase>() with scoped(EndpointScope.Default).singleton {
            ContentManifestDownloadUseCase(
                enqueueBlobDownloadClientUseCase = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                httpClient = instance(),
                json = instance(),
                cacheTmpPath = File(
                    applicationContext.httpPersistentFilesDir,
                    UstadCacheBuilder.DEFAULT_SUBPATH_WORK
                ).absolutePath.requireFileSeparatorSuffix(),
            )
        }

        bind<EnqueueContentManifestDownloadJobUseCaseAndroid>() with scoped(EndpointScope.Default).singleton {
            EnqueueContentManifestDownloadJobUseCaseAndroid(
                appContext = applicationContext,
                endpoint = context,
                db = instance(tag = DoorTag.TAG_DB),
            )
        }

        bind<ContentEntryVersionServerUseCase>() with scoped(EndpointScope.Default).singleton {
            ContentEntryVersionServerUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance(tag = DoorTag.TAG_REPO),
                okHttpClient = instance(),
                json = instance(),
                onlyIfCached = false,
            )
        }

        bind<GetStoragePathForUrlUseCase>() with singleton {
            GetStoragePathForUrlUseCaseCommonJvm(
                okHttpClient = instance(),
                cache = instance(),
                tmpDir = instance(tag = DiTag.TAG_TMP_DIR),
            )
        }

        bind<GetVersionUseCase>() with singleton {
            GetVersionUseCaseAndroid(applicationContext)
        }

        bind<SetPasswordUseCase>() with scoped(EndpointScope.Default).singleton {
            SetPasswordUseCaseCommonJvm(
                authManager = instance()
            )
        }

        bind<ResolveXapiLaunchHrefUseCase>() with scoped(EndpointScope.Default).singleton {
            ResolveXapiLaunchHrefUseCase(
                activeRepo = instance(tag = DoorTag.TAG_REPO),
                httpClient = instance(),
                json = instance(),
                xppFactory = instance(tag = DiTag.XPP_FACTORY_NSAWARE),
            )
        }

        bind<EmbeddedHttpServer>() with singleton {
            EmbeddedHttpServer(
                port = 0,
                contentEntryVersionServerUseCase = {
                    di.on(it).direct.instance()
                },
                staticUmAppFilesDir = null,
                mimeTypeHelper = FileMimeTypeHelperImpl(),
            )
        }

        bind<GetHtmlContentDisplayEngineOptionsUseCase>() with singleton {
            GetHtmlContentDisplayEngineOptionsUseCase(HTML_CONTENT_OPTIONS_ANDROID)
        }

        bind<GetHtmlContentDisplayEngineUseCase>() with singleton {
            GetHtmlContentDisplayEngineUseCase(
                settings = instance(),
                getOptionsUseCase = instance(),
            )
        }

        bind<SetHtmlContentDisplayEngineUseCase>() with singleton {
            SetHtmlContentDisplayEngineUseCase(settings = instance())
        }

        bind<ValidateVideoFileUseCase>() with singleton {
            ValidateVideoFileUseCaseAndroid(appContext = applicationContext)
        }

        bind<GetShowPoweredByUseCase>() with singleton {
            GetShowPoweredByUseCase(
                applicationContext.appMetaData?.getBoolean(AppConfig.KEY_CONFIG_SHOW_POWERED_BY) ?: false,
            )
        }

        bind<SetClipboardStringUseCase>() with provider {
            SetClipboardStringUseCaseAndroid(applicationContext)
        }

        bind<GetDeveloperInfoUseCase>() with provider {
            GetDeveloperInfoUseCaseAndroid(applicationContext)
        }

        bind<DeleteContentEntryParentChildJoinUseCase>() with scoped(EndpointScope.Default).provider {
            DeleteContentEntryParentChildJoinUseCase(
                repoOrDb = instance(tag = DoorTag.TAG_REPO),
            )
        }

        bind<RestoreDeletedItemUseCase>() with scoped(EndpointScope.Default).provider {
            RestoreDeletedItemUseCase(
                repoOrDb = instance(tag = DoorTag.TAG_REPO),
            )
        }

        bind<DeletePermanentlyUseCase>() with scoped(EndpointScope.Default).provider {
            DeletePermanentlyUseCase(
                repoOrDb = instance(tag = DoorTag.TAG_REPO),
            )
        }

        bind<MakeContentEntryAvailableOfflineUseCase>() with scoped(EndpointScope.Default).singleton {
            MakeContentEntryAvailableOfflineUseCase(
                repo = instance(tag = DoorTag.TAG_REPO),
                nodeIdAndAuth = instance(),
                enqueueContentManifestDownloadUseCase = instance(),
            )
        }

        bind<CancelDownloadUseCase>() with scoped(EndpointScope.Default).provider {
            CancelDownloadUseCaseAndroid(
                appContext = applicationContext,
                endpoint = context,
                db = instance(tag = DoorTag.TAG_DB)
            )
        }

        bind<ShareTextUseCase>() with singleton {
            ShareTextUseCaseAndroid(applicationContext)
        }

        bind<SaveAndUploadLocalUrisUseCase>() with scoped(EndpointScope.Default).singleton {
            SaveAndUploadLocalUrisUseCase(
                saveLocalUrisAsBlobsUseCase = instance(),
                enqueueBlobUploadClientUseCase = instance(),
                activeDb = instance(tag = DoorTag.TAG_DB),
                activeRepo = instance(tag = DoorTag.TAG_REPO),
            )
        }

        bind<CancelBlobUploadClientUseCase>() with scoped(EndpointScope.Default).singleton {
            CancelBlobUploadClientUseCaseAndroid(
                appContext = applicationContext,
                endpoint = context,
                db = instance(tag = DoorTag.TAG_DB),
            )
        }

        bind<OpenBlobUseCase>() with scoped(EndpointScope.Default).singleton {
            OpenBlobUseCaseAndroid(
                appContext = applicationContext,
                getStoragePathForUrlUseCase = instance()
            )
        }

        bind<ValidateEmailUseCase>() with provider {
            ValidateEmailUseCase()
        }

        registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())

        val metadataPresetLang = appMetaData?.getString(APPCONFIG_KEY_PRESET_LANG)

        if(!metadataPresetLang.isNullOrEmpty()) {
            val settings: Settings = di.direct.instance()
            val presetActioned = settings.getStringOrNull(PREFKEY_ACTIONED_PRESET)
            if(presetActioned != true.toString()) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(metadataPresetLang))
                settings.putString(PREFKEY_ACTIONED_PRESET, true.toString())
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            di.direct.instance<EmbeddedHttpServer>().start()
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