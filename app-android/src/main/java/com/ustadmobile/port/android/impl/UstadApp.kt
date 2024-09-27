package com.ustadmobile.port.android.impl

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
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
import com.ustadmobile.core.db.ext.MIGRATION_169_170_CLIENT
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.*
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.domain.account.CreateNewLocalAccountUseCase
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
import com.ustadmobile.core.domain.compress.audio.CompressAudioUseCase
import com.ustadmobile.core.domain.compress.audio.CompressAudioUseCaseAndroid
import com.ustadmobile.core.domain.compress.image.CompressImageUseCase
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseAndroid
import com.ustadmobile.core.domain.compress.list.CompressListUseCase
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCase
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCaseAndroid
import com.ustadmobile.core.domain.contententry.delete.DeleteContentEntryParentChildJoinUseCase
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.CancelImportContentEntryUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CancelImportContentEntryUseCaseAndroid
import com.ustadmobile.core.domain.contententry.importcontent.CancelRemoteContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.DismissRemoteContentEntryImportErrorUseCase
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
import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCaseAndroid
import com.ustadmobile.core.domain.extractvideothumbnail.ExtractVideoThumbnailUseCase
import com.ustadmobile.core.domain.extractvideothumbnail.ExtractVideoThumbnailUseCaseAndroid
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCaseEmbeddedServer
import com.ustadmobile.core.domain.getdeveloperinfo.GetDeveloperInfoUseCase
import com.ustadmobile.core.domain.getdeveloperinfo.GetDeveloperInfoUseCaseAndroid
import com.ustadmobile.core.domain.interop.oneroster.OneRosterEndpoint
import com.ustadmobile.core.domain.interop.oneroster.OneRosterHttpServerUseCase
import com.ustadmobile.core.domain.passkey.SavePersonPasskeyUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.share.ShareTextUseCase
import com.ustadmobile.core.domain.share.ShareTextUseCaseAndroid
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.domain.storage.CachePathsProviderAndroid
import com.ustadmobile.core.domain.storage.GetAndroidSdCardDirUseCase
import com.ustadmobile.core.domain.storage.GetOfflineStorageAvailableSpace
import com.ustadmobile.core.domain.storage.GetOfflineStorageAvailableSpaceAndroid
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCaseAndroid
import com.ustadmobile.core.domain.storage.GetOfflineStorageSettingUseCase
import com.ustadmobile.core.domain.storage.SetOfflineStorageSettingUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseAndroid
import com.ustadmobile.core.domain.upload.ChunkedUploadClientChunkGetterUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCase
import com.ustadmobile.core.domain.xapi.StoreActivitiesUseCase
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.http.XapiHttpServerUseCase
import com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder.NonInteractiveContentXapiStatementRecorderFactory
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCaseAndroid
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCase
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCaseLocal
import com.ustadmobile.core.domain.xapi.state.DeleteXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.ListXapiStateIdsUseCase
import com.ustadmobile.core.domain.xapi.state.RetrieveXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.StoreXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.h5puserdata.H5PUserDataEndpointUseCase
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.config.BundleBuildConfig
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.LocaleSettingDelegateAndroid
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.APPCONFIG_KEY_PRESET_LANG
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.PREFKEY_ACTIONED_PRESET
import com.ustadmobile.core.impl.config.UstadBuildConfig
import com.ustadmobile.core.impl.nav.NavCommandExecutionTracker
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperAndroid
import com.ustadmobile.core.util.ext.appMetaData
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.util.ext.toNullIfBlank
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.headers.MimeTypeHelper
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
import rawhttp.core.RawHttp
import com.toughra.ustadmobile.BuildConfig
import com.ustadmobile.core.domain.localaccount.GetLocalAccountsSupportedUseCase

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
            val cachePathProvider: CachePathsProvider = instance()

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
                        tmpDirProvider = { File(cachePathProvider().tmpWorkPath.toString()) },
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

        bind<UstadBuildConfig>() with singleton {
            BundleBuildConfig(appMetaData)
        }

        bind<Settings>() with singleton {
            SharedPreferencesSettings(
                getSharedPreferences(UstadMobileSystemImpl.APP_PREFERENCES_NAME, Context.MODE_PRIVATE)
            )
        }

        bind<NodeIdAndAuth>() with scoped(LearningSpaceScope.Default).singleton {
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

        bind<SystemUrlConfig>() with singleton {
            SystemUrlConfig.fromUstadBuildConfig(instance())
        }

        bind<Json>() with singleton {
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
        }

        bind<XapiJson>() with singleton { XapiJson() }

        bind<XML>() with singleton {
            XML {
                defaultPolicy {
                    unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                }
            }
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


        //TODO: Add systemdb module as a dependency
        //TODO: Create SystemDbDataLayer following the same pattern
        //TODO: Bind the SystemDb (e.g. local)
        //TODO: to create repository - use SystemApiUrlConfig
        //TODO: Create a data class called SystemDbNodeIdAndAuth - with one member variable - type NodeIdAndAuth
        //Then use the same logic to create it as used for the main nodeidandauth
        //TODO: That will provide the SystemDb in the DI for the app client - then we can use
        // learningspaceinfo.findallaspagingsource etc. to list available learning spaces.

        bind<DbAndObservers>() with scoped(LearningSpaceScope.Default).singleton {
            val dbName = sanitizeDbNameFromUrl(context.url)


            val nodeIdAndAuth: NodeIdAndAuth = instance()

            Log.i("MigrateIssue", "Creating database name=$dbName")
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
                .addMigrations(MIGRATION_169_170_CLIENT)
                .build()

            Log.i("MigrateIssue", "Database built: name=$dbName")

            val cache: UstadCache = instance()

            DbAndObservers(
                db = db,
                updateCacheLockJoinUseCase = UpdateCacheLockJoinUseCase(
                    db = db,
                    cache = cache,
                )
            )
        }

        bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(LearningSpaceScope.Default).singleton {
            instance<DbAndObservers>().db
        }

        bind<UmAppDataLayer>() with scoped(LearningSpaceScope.Default).singleton {
            val db: UmAppDatabase = instance(tag = DoorTag.TAG_DB)
            val repo: UmAppDatabase? = if(!context.isLocal) {
                val nodeIdAndAuth: NodeIdAndAuth = instance()
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
            }else {
                null
            }

            UmAppDataLayer(
                localDb  = db,
                repository = repo,
            )
        }

        bind<UriHelper>() with singleton {
            UriHelperAndroid(applicationContext)
        }

        bind<XhtmlFixer>() with singleton {
            XhtmlFixerJsoup(xml = instance())
        }

        bind<CachePathsProvider>() with singleton {
            CachePathsProviderAndroid(
                appContext = applicationContext,
                getAndroidSdCardPathUseCase = instance(),
                getOfflineStorageSettingUseCase = instance()
            )
        }

        bind<UstadCache>() with singleton {
            val httpCacheDir =  applicationContext.httpPersistentFilesDir
            val storagePath = Path(httpCacheDir.absolutePath)


            UstadCacheBuilder(
                appContext = applicationContext,
                storagePath = storagePath,
                logger = NapierLoggingAdapter(),
                sizeLimit = { 100_000_000L },
                cachePathsProvider = instance(),
            ).build()
        }

        bind<ContentImportersManager>() with scoped(LearningSpaceScope.Default).singleton {
            val cache: UstadCache = instance()
            val uriHelper: UriHelper = instance()
            val xml: XML = instance()
            val xhtmlFixer: XhtmlFixer = instance()
            val db: UmAppDatabase = instance(tag = DoorTag.TAG_DB)
            val saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase = instance()
            val tmpRoot: File = instance(tag = DiTag.TAG_TMP_DIR)
            val contentImportTmpPath = Path(tmpRoot.absolutePath, "contentimport")
            val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase= instance()
            val mimeTypeHelper: MimeTypeHelper = instance()

            ContentImportersManager(
                buildList {
                    add(
                        EpubContentImporterCommonJvm(
                            learningSpace = context,
                            cache = cache,
                            db = db,
                            uriHelper = uriHelper,
                            xml = xml,
                            xhtmlFixer = xhtmlFixer,
                            tmpPath = contentImportTmpPath,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            json = instance(),
                            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                            compressListUseCase = instance(),
                            saveLocalUrisAsBlobsUseCase = instance(),
                        )
                    )
                    add(
                        XapiZipContentImporter(
                            learningSpace = context,
                            db = db,
                            cache = cache,
                            uriHelper = uriHelper,
                            json = instance(),
                            tmpPath = contentImportTmpPath,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            compressListUseCase = instance(),
                        )
                    )

                    add(
                        H5PContentImporter(
                            learningSpace = context,
                            db = db,
                            cache = cache,
                            uriHelper = uriHelper,
                            tmpPath = contentImportTmpPath,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            json = instance(),
                            compressListUseCase = instance(),
                            h5pInStream = {
                                applicationContext.assets.open("h5p/h5p-standalone-3.6.0.zip",
                                    AssetManager.ACCESS_STREAMING)
                            }
                        ),
                    )

                    add(
                        VideoContentImporterCommonJvm(
                            learningSpace = context,
                            validateVideoFileUseCase = instance(),
                            uriHelper = uriHelper,
                            cache = cache,
                            tmpPath = contentImportTmpPath,
                            db = db,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            json = instance(),
                            getStoragePathForUrlUseCase  = getStoragePathForUrlUseCase,
                            mimeTypeHelper = mimeTypeHelper,
                            compressUseCase = instance(),
                            extractVideoThumbnailUseCase = instance(),
                            saveLocalUrisAsBlobsUseCase = instance(),
                        )
                    )

                    add(
                        PdfContentImporterAndroid(
                            learningSpace = context,
                            cache = cache,
                            uriHelper = uriHelper,
                            db = db,
                            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                            json = instance(),
                            appContext = applicationContext,
                            tmpDir = File(contentImportTmpPath.toString()),
                            saveLocalUriAsBlobUseCase = instance(),
                        )
                    )
                }
            )
        }

        bind<CompressVideoUseCase>() with singleton {
            CompressVideoUseCaseAndroid(
                appContext = applicationContext,
                uriHelper = instance(),
            )
        }

        bind<CompressAudioUseCase>() with singleton {
            CompressAudioUseCaseAndroid(
                appContext = applicationContext,
                uriHelper = instance(),
            )
        }

        bind<CompressImageUseCase>() with singleton {
            CompressImageUseCaseAndroid(applicationContext)
        }

        bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
            XmlPullParserFactory.newInstance().also {
                it.isNamespaceAware = true
            }
        }

        bind<AuthManager>() with scoped(LearningSpaceScope.Default).singleton {
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



        bind<SaveLocalUrisAsBlobsUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            val rootTmpDir: File = instance(tag = DiTag.TAG_TMP_DIR)
            SaveLocalUrisAsBlobsUseCaseJvm(
                learningSpace = context,
                cache = instance(),
                uriHelper = instance(),
                tmpDir = Path(rootTmpDir.absolutePath, "savelocaluriaslblobtmp"),
                fileSystem = SystemFileSystem,
                deleteUrisUseCase = instance(),
            )
        }

        bind<MimeTypeHelper>() with singleton {
            FileMimeTypeHelperImpl()
        }

        bind<SaveLocalUriAsBlobAndManifestUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SaveLocalUriAsBlobAndManifestUseCaseJvm(
                saveLocalUrisAsBlobsUseCase = instance(),
                mimeTypeHelper = instance(),
            )
        }

        bind<EnqueueBlobUploadClientUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            EnqueueBlobUploadClientUseCaseAndroid(
                appContext = applicationContext,
                learningSpace = context,
                db = on(context).instance(tag = DoorTag.TAG_DB),
                cache = instance(),
            )
        }

        bind<BlobUploadClientUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            val repo = instance<UmAppDataLayer>().repository
                ?: throw IllegalStateException("Cannot BlobUploadClientUseCase for local endpoint")

            BlobUploadClientUseCaseJvm(
                chunkedUploadUseCase = on(context).instance(),
                httpClient = instance(),
                httpCache = instance(),
                json = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                repo = repo,
                learningSpace = context,
            )
        }

        bind<EnqueueSavePictureUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            EnqueueSavePictureUseCaseAndroid(
                appContext = applicationContext,
                learningSpace = context,
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

        bind<SavePictureUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SavePictureUseCase(
                saveLocalUrisAsBlobUseCase = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                enqueueBlobUploadClientUseCase = takeIf { !context.isLocal }?.instance(),
                compressImageUseCase = instance(),
                deleteUrisUseCase = instance(),
                getStoragePathForUrlUseCase = instance(),
            )
        }

        bind<UpdateFailedTransferJobUseCase>() with scoped(LearningSpaceScope.Default).provider {
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

        bind<ContentEntryGetMetaDataFromUriUseCase>() with scoped(LearningSpaceScope.Default).provider {
            ContentEntryGetMetaDataFromUriUseCaseCommonJvm(
                importersManager = instance()
            )
        }

        bind<EnqueueContentEntryImportUseCase>() with scoped(LearningSpaceScope.Default).provider {
            EnqueueImportContentEntryUseCaseAndroid(
                db = instance(tag = DoorTag.TAG_DB),
                appContext = applicationContext,
                learningSpace = context,
                enqueueRemoteImport = EnqueueImportContentEntryUseCaseRemote(
                    learningSpace = context,
                    httpClient = instance(),
                    json = instance(),
                )
            )
        }

        bind<CancelImportContentEntryUseCase>() with scoped(LearningSpaceScope.Default).provider {
            CancelImportContentEntryUseCaseAndroid(
                appContext = applicationContext,
                endpoint = context,
            )
        }

        bind<ImportContentEntryUseCase>() with scoped(LearningSpaceScope.Default).provider {
            ImportContentEntryUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                importersManager = instance(),
                enqueueBlobUploadClientUseCase = instance(),
                createRetentionLocksForManifestUseCase = instance(),
                httpClient = instance(),
                json = instance(),
            )
        }

        bind<CreateRetentionLocksForManifestUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            CreateRetentionLocksForManifestUseCaseCommonJvm(
                cache = instance()
            )
        }

        bind<BlobDownloadClientUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            BlobDownloadClientUseCaseCommonJvm(
                okHttpClient = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().requireRepository(),
                httpCache = instance(),
            )
        }

        bind<EnqueueBlobDownloadClientUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            EnqueueBlobDownloadClientUseCaseAndroid(
                appContext = applicationContext,
                learningSpace = context,
                db = instance(tag = DoorTag.TAG_DB)
            )
        }

        bind<ContentManifestDownloadUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            val cachePathsProvider: CachePathsProvider = instance()

            ContentManifestDownloadUseCase(
                enqueueBlobDownloadClientUseCase = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                httpClient = instance(),
                json = instance(),
                cacheTmpPath = { cachePathsProvider().tmpWorkPath.toString() }
            )
        }

        bind<EnqueueContentManifestDownloadJobUseCaseAndroid>() with scoped(LearningSpaceScope.Default).singleton {
            EnqueueContentManifestDownloadJobUseCaseAndroid(
                appContext = applicationContext,
                learningSpace = context,
                db = instance(tag = DoorTag.TAG_DB),
            )
        }

        bind<ContentEntryVersionServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ContentEntryVersionServerUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
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

        bind<SetPasswordUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SetPasswordUseCaseCommonJvm(
                authManager = instance()
            )
        }

        bind<ResolveXapiLaunchHrefUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ResolveXapiLaunchHrefUseCase(
                activeRepoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
                httpClient = instance(),
                json = instance(),
                xppFactory = instance(tag = DiTag.XPP_FACTORY_NSAWARE),
                learningSpace = context,
                accountManager = instance(),
                getApiUrlUseCase = instance(),
                resumeOrStartXapiSessionUseCase = instance(),
            )
        }

        bind<ResumeOrStartXapiSessionUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ResumeOrStartXapiSessionUseCaseLocal(
                activeDb = instance(tag = DoorTag.TAG_DB),
                activeRepo = instance<UmAppDataLayer>().repository,
                xxStringHasher= instance(),
            )
        }

        bind<EmbeddedHttpServer>() with singleton {
            EmbeddedHttpServer(
                port = 0,
                contentEntryVersionServerUseCase = {
                    di.on(it).direct.instance()
                },
                xapiServerUseCase = {
                    di.on(it).direct.instance()
                },
                staticUmAppFilesDir = null,
                mimeTypeHelper = FileMimeTypeHelperImpl(),
            )
        }

        bind<XapiHttpServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            XapiHttpServerUseCase(
                statementResource = instance(),
                retrieveXapiStateUseCase = instance(),
                storeXapiStateUseCase = instance(),
                listXapiStateIdsUseCase = instance(),
                deleteXapiStateRequest = instance(),
                h5PUserDataEndpointUseCase = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                xapiJson = instance(),
                learningSpace = context,
                xxStringHasher = instance(),
            )
        }

        bind<H5PUserDataEndpointUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            H5PUserDataEndpointUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                xxStringHasher = instance(),
                xxHasher64Factory = instance(),
                xapiJson = instance(),
            )
        }

        bind<StoreXapiStateUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            StoreXapiStateUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                xapiJson = instance(),
                xxHasher64Factory = instance(),
                xxStringHasher = instance(),
                learningSpace = context,
            )
        }

        bind<RetrieveXapiStateUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            RetrieveXapiStateUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                xapiJson = instance(),
                xxStringHasher = instance(),
                xxHasher64Factory = instance(),
            )
        }

        bind<ListXapiStateIdsUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ListXapiStateIdsUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                xxStringHasher = instance(),
            )
        }

        bind<DeleteXapiStateUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            DeleteXapiStateUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                xxStringHasher = instance(),
                xxHasher64Factory = instance(),
                learningSpace = context,
            )
        }

        bind<GetApiUrlUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            GetApiUrlUseCaseEmbeddedServer(
                embeddedServer = instance(),
                learningSpace = context,
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
            ValidateVideoFileUseCase(
                extractMediaMetadataUseCase = instance()
            )
        }

        bind<ExtractMediaMetadataUseCase>() with singleton {
            ExtractMediaMetadataUseCaseAndroid(applicationContext)
        }

        bind<GetShowPoweredByUseCase>() with singleton {
            GetShowPoweredByUseCase(
                applicationContext.appMetaData?.getBoolean(UstadBuildConfig.KEY_CONFIG_SHOW_POWERED_BY) ?: false,
            )
        }
        bind<GetLocalAccountsSupportedUseCase>() with singleton {
            GetLocalAccountsSupportedUseCase(true)
        }
        bind<SetClipboardStringUseCase>() with provider {
            SetClipboardStringUseCaseAndroid(applicationContext)
        }

        bind<GetDeveloperInfoUseCase>() with provider {
            GetDeveloperInfoUseCaseAndroid(applicationContext)
        }

        bind<DeleteContentEntryParentChildJoinUseCase>() with scoped(LearningSpaceScope.Default).provider {
            DeleteContentEntryParentChildJoinUseCase(
                repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
            )
        }

        bind<RestoreDeletedItemUseCase>() with scoped(LearningSpaceScope.Default).provider {
            RestoreDeletedItemUseCase(
                repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
            )
        }

        bind<DeletePermanentlyUseCase>() with scoped(LearningSpaceScope.Default).provider {
            DeletePermanentlyUseCase(
                repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
            )
        }
        bind<SavePersonPasskeyUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SavePersonPasskeyUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
            )
        }
        bind<MakeContentEntryAvailableOfflineUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            MakeContentEntryAvailableOfflineUseCase(
                repo = instance<UmAppDataLayer>().requireRepository(),
                nodeIdAndAuth = instance(),
                enqueueContentManifestDownloadUseCase = instance(),
            )
        }

        bind<CancelDownloadUseCase>() with scoped(LearningSpaceScope.Default).provider {
            CancelDownloadUseCaseAndroid(
                appContext = applicationContext,
                learningSpace = context,
                db = instance(tag = DoorTag.TAG_DB)
            )
        }

        bind<ShareTextUseCase>() with singleton {
            ShareTextUseCaseAndroid(applicationContext)
        }

        bind<SaveAndUploadLocalUrisUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SaveAndUploadLocalUrisUseCase(
                saveLocalUrisAsBlobsUseCase = instance(),
                enqueueBlobUploadClientUseCase = instance(),
                activeDb = instance(tag = DoorTag.TAG_DB),
                activeRepo = instance<UmAppDataLayer>().repository,
            )
        }

        bind<CancelBlobUploadClientUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            CancelBlobUploadClientUseCaseAndroid(
                appContext = applicationContext,
                learningSpace = context,
                db = instance(tag = DoorTag.TAG_DB),
            )
        }

        bind<OpenBlobUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            OpenBlobUseCaseAndroid(
                appContext = applicationContext,
                getStoragePathForUrlUseCase = instance()
            )
        }

        bind<ValidateEmailUseCase>() with provider {
            ValidateEmailUseCase()
        }

        bind<CancelRemoteContentEntryImportUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            CancelRemoteContentEntryImportUseCase(
                learningSpace = context,
                httpClient = instance(),
                repo = instance<UmAppDataLayer>().requireRepository(),
            )
        }

        bind<DismissRemoteContentEntryImportErrorUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            DismissRemoteContentEntryImportErrorUseCase(
                learningSpace = context,
                httpClient = instance(),
                repo = instance<UmAppDataLayer>().requireRepository(),
            )
        }

        bind<CompressListUseCase>() with singleton {
            CompressListUseCase(
                compressVideoUseCase = instance(),
                compressImageUseCase = instance(),
                compressAudioUseCase = instance(),
                mimeTypeHelper = instance(),
            )
        }

        bind<ExtractVideoThumbnailUseCase>() with singleton {
            ExtractVideoThumbnailUseCaseAndroid(applicationContext)
        }

        bind<GetAndroidSdCardDirUseCase>() with singleton {
            GetAndroidSdCardDirUseCase(applicationContext)
        }

        bind<GetOfflineStorageOptionsUseCase>() with singleton {
            GetOfflineStorageOptionsUseCaseAndroid(
                getAndroidSdCardDirUseCase = instance()
            )
        }

        bind<GetOfflineStorageSettingUseCase>() with singleton {
            GetOfflineStorageSettingUseCase(
                getOfflineStorageOptionsUseCase = instance(),
                settings = instance(),
            )
        }

        bind<SetOfflineStorageSettingUseCase>() with singleton {
            SetOfflineStorageSettingUseCase(settings = instance())
        }

        bind<GetOfflineStorageAvailableSpace>() with singleton {
            GetOfflineStorageAvailableSpaceAndroid(
                getAndroidSdCardDirUseCase = instance(),
                appContext = applicationContext,
            )
        }

        bind<RawHttp>() with singleton {
            RawHttp()
        }

        bind<OneRosterEndpoint>() with scoped(LearningSpaceScope.Default).singleton {
            OneRosterEndpoint(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                learningSpace = context,
                xxHasher = instance(),
                json = instance(),
            )
        }

        bind<OneRosterHttpServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            OneRosterHttpServerUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                json = instance(),
                oneRosterEndpoint = instance(),
            )
        }

        bind<XXHasher64Factory>() with singleton {
            XXHasher64FactoryCommonJvm()
        }

        bind<XXStringHasher>() with singleton {
            XXStringHasherCommonJvm()
        }

        bind<StoreActivitiesUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            StoreActivitiesUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
            )
        }

        bind<XapiStatementResource>() with scoped(LearningSpaceScope.Default).singleton {
            XapiStatementResource(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
                xxHasher = instance(),
                learningSpace = context,
                xapiJson = instance(),
                hasherFactory = instance(),
                storeActivitiesUseCase = instance(),
            )
        }

        bind<SaveStatementOnClearUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SaveStatementOnClearUseCaseAndroid(
                appContext = applicationContext,
                learningSpace = context,
                json = instance(),
            )
        }

        bind<NonInteractiveContentXapiStatementRecorderFactory>() with scoped(LearningSpaceScope.Default).provider {
            NonInteractiveContentXapiStatementRecorderFactory(
                saveStatementOnClearUseCase = instance(),
                saveStatementOnUnloadUseCase = null,
                xapiStatementResource = instance(),
                learningSpace = context,
            )
        }

        bind<AddNewPersonUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            AddNewPersonUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = instance<UmAppDataLayer>().repository,
            )
        }

        bind<CreateNewLocalAccountUseCase>() with singleton {
            CreateNewLocalAccountUseCase(di)
        }

        registerContextTranslator { account: UmAccount -> LearningSpace(account.endpointUrl) }
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

    }

}