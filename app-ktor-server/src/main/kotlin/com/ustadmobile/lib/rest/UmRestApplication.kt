package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.core.account.*
import com.ustadmobile.core.contentformats.ContentImportersDiModuleJvm
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.domain.account.SetPasswordServerUseCase
import com.ustadmobile.core.domain.account.SetPasswordUseCase
import com.ustadmobile.core.domain.account.SetPasswordUseCaseCommonJvm
import com.ustadmobile.core.domain.account.VerifyClientUserSessionUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.BlobUploadServerUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCaseCommonJvm
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.compress.audio.CompressAudioUseCase
import com.ustadmobile.core.domain.compress.audio.CompressAudioUseCaseSox
import com.ustadmobile.core.domain.compress.image.CompressImageUseCase
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseJvm
import com.ustadmobile.core.domain.compress.list.CompressListUseCase
import com.ustadmobile.core.domain.compress.pdf.CompressPdfUseCase
import com.ustadmobile.core.domain.compress.pdf.CompressPdfUseCaseJvm
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCase
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCaseHandbrake
import com.ustadmobile.core.domain.compress.video.FindHandBrakeUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CancelImportContentEntryServerUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CancelImportContentEntryUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CancelImportContentEntryUseCaseJvm
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueImportContentEntryUseCaseJvm
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExtractMediaMetadataUseCaseMediaInfo
import com.ustadmobile.core.domain.extractvideothumbnail.ExtractVideoThumbnailUseCase
import com.ustadmobile.core.domain.extractvideothumbnail.ExtractVideoThumbnailUseCaseJvm
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCaseDirect
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonStatusMap
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCaseImpl
import com.ustadmobile.core.domain.person.bulkadd.EnqueueBulkAddPersonServerUseCase
import com.ustadmobile.core.domain.person.bulkadd.EnqueueBulkAddPersonUseCase
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorJvm
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.phonenumber.PhoneNumberUtilJvm
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.domain.usersession.ValidateUserSessionOnServerUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCase
import com.ustadmobile.core.domain.xapi.StoreActivitiesUseCase
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.http.XapiHttpServerUseCase
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCase
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCaseLocal
import com.ustadmobile.core.domain.xapi.state.DeleteXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.ListXapiStateIdsUseCase
import com.ustadmobile.core.domain.xapi.state.RetrieveXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.StoreXapiStateUseCase
import com.ustadmobile.core.domain.xapi.state.h5puserdata.H5PUserDataEndpointUseCase
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.*
import com.ustadmobile.lib.rest.ext.*
import com.ustadmobile.lib.rest.messaging.MailProperties
import com.ustadmobile.lib.util.ext.bindDataSourceIfNotExisting
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.github.aakira.napier.Napier
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import org.kodein.di.*
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import java.io.File
import javax.naming.InitialContext
import com.ustadmobile.door.util.NodeIdAuthCache
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderJvm
import com.ustadmobile.core.schedule.initQuartzDb
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.door.http.DoorHttpServerConfig
import com.ustadmobile.lib.rest.dimodules.makeJvmBackendDiModule
import com.ustadmobile.lib.rest.api.blob.BlobUploadServerRoute
import com.ustadmobile.lib.rest.api.content.ContentEntryVersionRoute
import com.ustadmobile.lib.rest.domain.contententry.getmetadatafromuri.ContentEntryGetMetadataServerUseCase
import com.ustadmobile.lib.rest.api.contentupload.ContentUploadRoute
import com.ustadmobile.lib.rest.api.contentupload.UPLOAD_TMP_SUBDIR
import com.ustadmobile.lib.rest.domain.account.SetPasswordRoute
import com.ustadmobile.lib.rest.mediahelpers.MissingMediaProgramsException
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import com.ustadmobile.lib.util.SysPathUtil
import io.ktor.server.http.content.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import org.kodein.di.ktor.di
import java.util.*
import com.ustadmobile.core.logging.LogbackAntiLog
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.isWindowsOs
import com.ustadmobile.door.log.NapierDoorLogger
import com.ustadmobile.lib.rest.domain.contententry.importcontent.ContentEntryImportJobRoute
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceRoute
import com.ustadmobile.lib.rest.domain.learningspace.create.CreateLearningSpaceUseCase
import com.ustadmobile.lib.rest.domain.person.bulkadd.BulkAddPersonRoute
import com.ustadmobile.lib.rest.domain.xapi.XapiRoute
import com.ustadmobile.lib.rest.domain.xapi.savestatementonclear.SaveStatementOnUnloadRoute
import com.ustadmobile.lib.rest.domain.xapi.session.ResumeOrStartXapiSessionRoute
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.headers.MimeTypeHelper
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import org.kodein.di.ktor.closestDI
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery

const val TAG_UPLOAD_DIR = 10

@Suppress("unused")
const val CONF_DBMODE_VIRTUALHOST = "virtualhost"

const val CONF_DBMODE_SINGLETON = "singleton"

const val CONF_GOOGLE_API = "secret"

const val CONF_KEY_SITE_URL = "ktor.ustad.siteUrl"

const val CONF_KEY_URL_PREFIX = "ktor.ustad.urlPrefix"

/**
 * List of external commands (e.g. media converters) that must be found or have locations specified
 */
val REQUIRED_EXTERNAL_COMMANDS = emptyList<String>()

/**
 * List of prefixes which are always answered by the KTOR server. When using JsDev proxy mode, any
 * other url will be sent to the JS dev proxy
 */
val KTOR_SERVER_ROUTES = listOf(
    "/UmAppDatabase", "/config",
    "/ContainerEntryList", "/ContainerEntryFile", "/auth", "/ContainerMount",
    "/Site", "/import", "/contentupload", "/websocket", "/api", "/staticfiles"
)


/**
 * Returns an identifier that is used as a subdirectory for data storage (e.g. attachments,
 * containers, etc).
 */
fun LearningSpace.identifier(
    dbMode: String,
    singletonName: String = CONF_DBMODE_SINGLETON
) = if(dbMode == CONF_DBMODE_SINGLETON) {
    singletonName
}else {
    sanitizeDbNameFromUrl(url)
}

@Suppress("unused") // This is used as the KTOR server main module via application.conf
fun Application.umRestApplication(
    dbModeOverride: String? = null,
) {
    val appConfig = environment.config

    val siteUrl = environment.config.propertyOrNull(CONF_KEY_SITE_URL)?.getString()

    val sitePrefix = environment.config.propertyOrNull(CONF_KEY_URL_PREFIX)?.getString()

    val dbMode = dbModeOverride ?:  appConfig.propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON

    val ktorAppHome = ktorAppHomeDir()

//    if(dbMode != CONF_DBMODE_VIRTUALHOST && siteUrl.isNullOrBlank()) {
//        val likelyAddr = NetworkInterface.getNetworkInterfaces().toList().filter {
//            !it.isLoopback
//        }.flatMap { netInterface ->
//            netInterface.inetAddresses.toList().filter { it !is Inet6Address }
//        }.firstOrNull()?.let { "http://${it.hostAddress}:${appConfig.port}/"} ?: ""
//
//        throw SiteConfigException("ERROR: Site URL is not set. You MUST specify the site url e.g. $likelyAddr \n" +
//                "Please specify using the url parameter in command line e.g. add " +
//                "--siteUrl $likelyAddr \n" +
//                "to the command you are running or \n" +
//                "set this in the config file e.g. uncomment siteUrl and set as siteUrl = \"$likelyAddr\"")
//    }

    val mediaInfoFile = SysPathUtil.findCommandInPath(
        commandName = "mediainfo",
        manuallySpecifiedLocation = appConfig.commandFileProperty("mediainfo"),
    )

    val handBrakeCliCommand = runBlocking {
        FindHandBrakeUseCase(
            specifiedLocation = appConfig.commandFileProperty("handbrakecli")?.absolutePath
        ).invoke()
    }

    val soxCommand = CompressAudioUseCaseSox.findSox()

    if(mediaInfoFile == null || !mediaInfoFile.exists()) {
        throw MissingMediaProgramsException("Cannot find mediainfo command you must install it: \n" +
                "On Ubuntu: apt-get install mediainfo\n" +
                "On Windows: winget install -e --id MediaArea.MediaInfo")
    }

    if(handBrakeCliCommand == null) {
        throw MissingMediaProgramsException("Cannot find HandBrakeCLI or version is less than 1.6\n" +
                "On Ubuntu 23.04+: apt-get-install handbrake-cli\n" +
                "On Ubuntu (pre 23.04): apt-get install flatpak, download HandBrakeCLI from handbrake.fr, " +
                "flatpak install /path/where/downloaded/HandBrakeCLI-1.7.3-x86_64.flatpak" +
                "on Windows: winget install -e --id HandBrake.HandBrake.CLI")
    }

    if(soxCommand == null) {
        throw MissingMediaProgramsException("Cannot find SoX" +
                "On Ubuntu: apt-get install sox libsox-fmt-all\n" +
                "On Windows: Download and install from SoX website: https://sourceforge.net/projects/sox/files/sox/14.4.2/")
    }

    if(!NativeDiscovery().discover()) {
        throw MissingMediaProgramsException("Cannot find VLC.\n" +
                "On Ubuntu: apt-get install vlc\n" +
                "On Windows: Download and install a **64bit** version from videolan.org")
    }

    val commandsDir = File(ktorAppHome, "commands")
    val mpg123Command = if(isWindowsOs()) {
        val extraSearchPaths = listOf("mpg123", "mpg123-1.32.6-x86").map { commandSubDir ->
            File(commandsDir, commandSubDir).absolutePath
        }.joinToString(separator = File.pathSeparator)

        SysPathUtil.findCommandInPath(
            commandName = "mpg123",
            extraSearchPaths = extraSearchPaths,
        ).also {
            if(it == null) {
                throw MissingMediaProgramsException("Cannot find mpg123. This is required on Windows.\n" +
                        "Download it from https://www.mpg123.de/download/win32/1.32.6/\n" +
                        "Then unzip into ${commandsDir.absolutePath}, or put it anywhere in your" +
                        "PATH environment variable, or manually specify location using ustad-server.conf")
            }
        }
    }else {
        null
    }

    //GhostScript - used for PDF compression (optional)
    val gsCommand = SysPathUtil.findCommandInPath(
        commandName = "gs",
        manuallySpecifiedLocation = appConfig.commandFileProperty("gs"),
    )

    val devMode = environment.config.propertyOrNull("ktor.ustad.devmode")?.getString().toBoolean()

    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    //Check for required external commands
    REQUIRED_EXTERNAL_COMMANDS.forEach { command ->
        if(!SysPathUtil.commandExists(command,
                manuallySpecifiedLocation = appConfig.commandFileProperty(command))
        ) {
            val message = "FATAL ERROR: Required external command \"$command\" not found in path or " +
                   "manually specified location does not exist. Please set it in application.conf"
            Napier.e(message)
            throw IllegalStateException(message)
        }
    }


    if (devMode) {
        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)
            allowHeader("X-nid")
            allowHeader("door-dbversion")
            allowHeader("door-node")
            anyHost()
        }
    }

    //Uncomment if needed -generates a lot of output
    //install(CallLogging)

    Napier.takeLogarithm()
    Napier.base(LogbackAntiLog())

    install(ContentNegotiation) {
        json(
            json = json,
            contentType = ContentType.Application.Json
        )
    }

    //Avoid sending the body of content if it has not changed since the client last requested it.
    install(ConditionalHeaders)

    val dataDirPath = environment.config.absoluteDataDir()

    fun String.replaceDbUrlVars(): String {
        return replace("(datadir)", dataDirPath.absolutePath)
    }

    dataDirPath.takeIf { !it.exists() }?.mkdirs()

    val apiKey = environment.config.propertyOrNull("ktor.ustad.googleApiKey")?.getString() ?: CONF_GOOGLE_API


    di {
        import(
            makeJvmBackendDiModule(
                config = environment.config, json = json
            )
        )
        import(ContentImportersDiModuleJvm)


        bind<StringProvider>() with singleton { StringProviderJvm(Locale.getDefault()) }

        bind<File>(tag = TAG_UPLOAD_DIR) with scoped(LearningSpaceScope.Default).singleton {
            val mainTmpDir = instance<File>(tag = DiTag.TAG_TMP_DIR)
            File(mainTmpDir, context.identifier(dbMode)).also {
                it.takeIf { !it.exists() }?.mkdirs()
            }
        }

        bind<NodeIdAuthCache>() with scoped(LearningSpaceScope.Default).singleton {
            instance<UmAppDatabase>(tag = DoorTag.TAG_DB).nodeIdAuthCache
        }

        bind<String>(tag = DiTag.TAG_GOOGLE_API) with singleton {
            apiKey
        }

        bind<Gson>() with singleton { Gson() }

        bind<FileMimeTypeHelperImpl>() with singleton {
            FileMimeTypeHelperImpl()
        }

        bind<MimeTypeHelper>() with singleton {
            instance<FileMimeTypeHelperImpl>()
        }

        bind<UriHelper>() with singleton {
            UriHelperJvm(
                mimeTypeHelperImpl = instance(),
                httpClient = instance(),
                okHttpClient = instance(),
            )
        }


        bind<Scheduler>() with singleton {
            val dbProperties = environment.config
                .databasePropertiesFromSection(
                    section = "quartz",
                    defaultUrl = "jdbc:hsqldb:file:(datadir)/quartz",
                    defaultDriver = "org.hsqldb.jdbc.JDBCDriver",
                    defaultUser = "SA",
                )
            dbProperties.setProperty("url", dbProperties.getProperty("url").replaceDbUrlVars())

            InitialContext().apply {
                bindDataSourceIfNotExisting("quartzds", dbProperties)
                initQuartzDb("java:/comp/env/jdbc/quartzds")
            }
            StdSchedulerFactory.getDefaultScheduler().also {
                it.context.put("di", di)
            }
        }

        bind<File>(tag = DiTag.TAG_TMP_DIR) with singleton {
            File(dataDirPath, "tmp")
        }

        bind<File>(tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR) with scoped(LearningSpaceScope.Default).singleton {
            val mainTmpDir = instance<File>(tag = DiTag.TAG_TMP_DIR)

            File(mainTmpDir, UPLOAD_TMP_SUBDIR).also {
                if(!it.exists())
                    it.mkdirs()
            }
        }

        bind<ContentEntryGetMetadataServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            val uploadDir: File = instance(DiTag.TAG_FILE_UPLOAD_TMP_DIR)
            ContentEntryGetMetadataServerUseCase(
                uploadDir = uploadDir,
                importersManager = on(context).instance(),
                json = instance()
            )
        }

        bind<BlobUploadServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            BlobUploadServerUseCase(
                httpCache = instance(),
                tmpDir = Path(
                    File(instance<File>(tag = DiTag.TAG_TMP_DIR), "blob-uploads-tmp").absolutePath.toString()
                ),
                json = instance(),
                saveLocalUrisAsBlobsUseCase = instance(),
            )
        }

        bind<ImportContentEntryUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ImportContentEntryUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                importersManager = instance(),
                json = instance(),
            )
        }

        bind<SaveLocalUrisAsBlobsUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            val rootTmpDir: File = instance(tag = DiTag.TAG_TMP_DIR)
            SaveLocalUrisAsBlobsUseCaseJvm(
                learningSpace = context,
                cache = instance(),
                uriHelper = instance(),
                tmpDir = Path(
                    File(rootTmpDir, "save-local-uris").absolutePath.toString()
                ),
                deleteUrisUseCase = instance()
            )
        }

        bind<SaveLocalUriAsBlobAndManifestUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SaveLocalUriAsBlobAndManifestUseCaseJvm(
                saveLocalUrisAsBlobsUseCase = instance(),
                mimeTypeHelper = instance(),
            )
        }

        bind<ContentEntryVersionServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ContentEntryVersionServerUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
                okHttpClient = instance(),
                json = instance(),
                onlyIfCached = true,
            )
        }

        bind<IsTempFileCheckerUseCase>() with singleton {
            IsTempFileCheckerUseCaseJvm(
                tmpRootDir = instance<File>(tag = DiTag.TAG_TMP_DIR)
            )
        }

        bind<DeleteUrisUseCase>() with singleton {
            DeleteUrisUseCaseCommonJvm(
                isTempFileCheckerUseCase = instance()
            )
        }

        bind<SetPasswordUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SetPasswordUseCaseCommonJvm(
                authManager = instance()
            )
        }

        bind<ValidateUserSessionOnServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ValidateUserSessionOnServerUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                nodeIdAuthCache = instance(),
            )
        }

        bind<SetPasswordServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            SetPasswordServerUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                setPasswordUseCase = instance(),
                validateUserSessionOnServerUseCase = instance()
            )
        }

        bind<GetStoragePathForUrlUseCase>() with singleton {
            GetStoragePathForUrlUseCaseCommonJvm(
                okHttpClient = instance(),
                cache = instance(),
                tmpDir = instance(tag = DiTag.TAG_TMP_DIR)
            )
        }

        bind<EnqueueContentEntryImportUseCase>() with scoped(LearningSpaceScope.Default).provider {
            EnqueueImportContentEntryUseCaseJvm(
                db = instance(tag = DoorTag.TAG_DB),
                scheduler = instance(),
                learningSpace = context,
                enqueueRemoteImport = null
            )
        }

        bind<ExecuteMediaInfoUseCase>() with provider {
            ExecuteMediaInfoUseCase(
                mediaInfoPath = mediaInfoFile.absolutePath,
                workingDir = ktorAppHomeDir(),
                json = instance(),
            )
        }

        bind<ExtractMediaMetadataUseCase>() with provider {
            ExtractMediaMetadataUseCaseMediaInfo(
                executeMediaInfoUseCase = instance(),
                getStoragePathForUrlUseCase = instance(),
            )
        }

        bind<CompressVideoUseCase>() with singleton {
            CompressVideoUseCaseHandbrake(
                handbrakeCommand = handBrakeCliCommand.command,
                workDir = instance<File>(tag = DiTag.TAG_TMP_DIR),
                json = instance(),
                extractMediaMetadataUseCase = instance(),
            )
        }

        bind<CompressAudioUseCase>() with singleton {
            CompressAudioUseCaseSox(
                soxPath = soxCommand.absolutePath,
                executeMediaInfoUseCase = instance(),
                mpg123Path = mpg123Command?.absolutePath,
                workDir = instance<File>(tag = DiTag.TAG_TMP_DIR),
            )
        }

        gsCommand?.also {
            bind<CompressPdfUseCase>() with singleton {
                CompressPdfUseCaseJvm(
                    gsPath = it,
                    workDir = instance<File>(tag = DiTag.TAG_TMP_DIR),
                )
            }
        }

        bind<ValidateVideoFileUseCase>() with provider {
            ValidateVideoFileUseCase(
                extractMediaMetadataUseCase = instance(),
            )
        }

        bind<AddNewPersonUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            AddNewPersonUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
            )
        }

        bind<BulkAddPersonsUseCase>() with scoped(LearningSpaceScope.Default).provider {
            BulkAddPersonsUseCaseImpl(
                addNewPersonUseCase = instance(),
                validateEmailUseCase  = instance(),
                validatePhoneNumUseCase = instance(),
                authManager = instance(),
                enrolUseCase = instance(),
                activeDb = instance(tag = DoorTag.TAG_DB),
                activeRepo = null,
            )
        }

        bind<ValidateEmailUseCase>() with provider {
            ValidateEmailUseCase()
        }

        bind<IPhoneNumberUtil>() with provider {
            PhoneNumberUtilJvm(PhoneNumberUtil.getInstance())
        }

        bind<PhoneNumValidatorUseCase>() with provider {
            PhoneNumValidatorJvm(
                iPhoneNumberUtil = instance()
            )
        }

        bind<EnrolIntoCourseUseCase>() with scoped(LearningSpaceScope.Default).provider {
            EnrolIntoCourseUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
            )
        }

        bind<VerifyClientUserSessionUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            VerifyClientUserSessionUseCase(
                nodeIdAndAuthCache = instance(),
                db = instance(tag = DoorTag.TAG_DB),
            )
        }

        bind<EnqueueBulkAddPersonServerUseCase>() with scoped(LearningSpaceScope.Default).provider {
            EnqueueBulkAddPersonServerUseCase(
                verifyClientSessionUseCase = instance(),
                enqueueBulkAddPersonUseCase = instance(),
                activeDb = instance(tag = DoorTag.TAG_DB),
            )
        }

        bind<EnqueueBulkAddPersonUseCase>() with scoped(LearningSpaceScope.Default).provider {
            EnqueueBulkAddPersonUseCase(
                scheduler = instance(),
                learningSpace = context,
                tmpDir = instance(tag = DiTag.TAG_TMP_DIR),
            )
        }

        bind<BulkAddPersonStatusMap>() with scoped(LearningSpaceScope.Default).singleton {
            BulkAddPersonStatusMap()
        }

        bind<CancelImportContentEntryUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            CancelImportContentEntryUseCaseJvm(
                scheduler = instance(),
                learningSpace = context,
            )
        }

        bind<CancelImportContentEntryServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            CancelImportContentEntryServerUseCase(
                cancelImportContentEntryUseCase = instance(),
                validateUserSessionOnServerUseCase = instance(),
                db = instance(tag = DoorTag.TAG_DB),
                learningSpace = context,
            )
        }

        bind<CompressImageUseCase>() with singleton {
            CompressImageUseCaseJvm()
        }

        bind<CompressListUseCase>() with singleton {
            CompressListUseCase(
                compressVideoUseCase = instance(),
                mimeTypeHelper = instance(),
                compressImageUseCase = instance(),
                compressAudioUseCase = instance(),
            )
        }

        bind<ExtractVideoThumbnailUseCase>() with singleton {
            ExtractVideoThumbnailUseCaseJvm()
        }

        bind<XXStringHasher>() with singleton {
            XXStringHasherCommonJvm()
        }

        bind<XXHasher64Factory>() with singleton {
            XXHasher64FactoryCommonJvm()
        }

        bind<StoreActivitiesUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            StoreActivitiesUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
            )
        }

        bind<XapiStatementResource>() with scoped(LearningSpaceScope.Default).singleton {
            XapiStatementResource(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
                xxHasher = instance(),
                learningSpace = context,
                xapiJson = instance(),
                hasherFactory = instance(),
                storeActivitiesUseCase = instance(),
            )
        }

        bind<ResumeOrStartXapiSessionUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ResumeOrStartXapiSessionUseCaseLocal(
                activeDb = instance(tag = DoorTag.TAG_DB),
                activeRepo = null,
                xxStringHasher = instance(),
            )
        }

        bind<XapiHttpServerUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            XapiHttpServerUseCase(
                statementResource = instance(),
                storeXapiStateUseCase = instance(),
                retrieveXapiStateUseCase = instance(),
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
                repo = null,
                xapiJson = instance(),
                xxStringHasher = instance(),
                xxHasher64Factory = instance(),
            )
        }

        bind<StoreXapiStateUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            StoreXapiStateUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
                xapiJson = instance(),
                xxHasher64Factory = instance(),
                xxStringHasher = instance(),
                learningSpace = context,
            )
        }

        bind<RetrieveXapiStateUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            RetrieveXapiStateUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
                xapiJson = instance(),
                xxStringHasher = instance(),
                xxHasher64Factory = instance(),
            )
        }

        bind<ListXapiStateIdsUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            ListXapiStateIdsUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
                xxStringHasher = instance(),
            )
        }

        bind<DeleteXapiStateUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            DeleteXapiStateUseCase(
                db = instance(tag = DoorTag.TAG_DB),
                repo = null,
                xxStringHasher = instance(),
                xxHasher64Factory = instance(),
                learningSpace = context,
            )
        }

        bind<GetApiUrlUseCase>() with scoped(LearningSpaceScope.Default).singleton {
            GetApiUrlUseCaseDirect(context)
        }

        bind<CreateLearningSpaceUseCase>() with singleton {
            CreateLearningSpaceUseCase(
                xxStringHasher = instance(),
                systemDb = instance()
            )
        }

        try {
            appConfig.config("mail")

            bind<MailProperties>() with singleton {
                MailProperties(appConfig.property("mail.from").getString(),
                    appConfig.toProperties(MailProperties.SMTP_PROPS))
            }

            bind<NotificationSender>() with singleton {
                NotificationSender(di)
            }

            bind<Authenticator>() with singleton {
                object: Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(
                            appConfig.property("mail.user").getString(),
                            appConfig.property("mail.auth").getString()
                        )
                    }
                }
            }
        }catch(e: Exception) {
            Napier.w("WARNING: Email sending not configured")
        }

        registerContextTranslator { call: ApplicationCall ->
            call.callLearningSpace
        }

        onReady {
            if(dbMode == CONF_DBMODE_SINGLETON && siteUrl != null) {
                val learningSpace = LearningSpace(siteUrl)
                val passwordFile = di.on(learningSpace).direct.instance<File>(tag = DiTag.TAG_ADMIN_PASS_FILE)

                /**
                 * Eager initialization only if the initial admin password needs generated. This
                 * avoids potential issue with startup script if this server starts before postgres
                 * is ready.
                 */
                if(!passwordFile.exists()) {
                    //Generate the admin username/password etc.
                    di.on(learningSpace).direct.instance<AuthManager>()

                    val db: UmAppDatabase by di.on(learningSpace).instance(tag = DoorTag.TAG_DB)
                    println("init db: $db")
                }
            }

            instance<Scheduler>().start()
            instance<SystemDb>()

            Runtime.getRuntime().addShutdownHook(Thread{
                instance<Scheduler>().shutdown()
            })
        }
    }

    //Ensure that older clients that make http calls to pages that no longer exist will not make
    // an infinite number of calls and exhaust their data bundle etc.
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { _: HttpStatusCode ->
            call.respondText("Not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
        }
    }

    val jsDevServer = appConfig.propertyOrNull("ktor.ustad.jsDevServer")?.getString()
    if(jsDevServer != null) {
        install(io.ktor.server.websocket.WebSockets)

        val effectiveKtorServerRoutes = if(sitePrefix != null) {
            KTOR_SERVER_ROUTES.map { UMFileUtil.joinPaths(sitePrefix, it) }
        }else {
            KTOR_SERVER_ROUTES
        }

        intercept(ApplicationCallPipeline.Setup) {
            val requestUri = call.request.uri.let {
                if(it.startsWith("//")) {
                    //This is an edge case with the ContainerFetcher. The ContainerFetcher uses //
                    // at the start of a URI. This workaround will be removed when ContainerFetcher
                    // is removed and replaced with Retriever.
                    it.removePrefix("/")
                }else {
                    it
                }
            }

            //If the request is not using the correct url as per system config, reject it and finish
            if(!context.urlMatchesConfig()) {
                call.respondRequestUrlNotMatchingSiteConfUrl()
                return@intercept finish()
            }

            //If the request is not matching any API route, then use the reverse proxy to send the
            // request to the javascript development server.
            if(!effectiveKtorServerRoutes.any { requestUri.startsWith(it) }) {
                call.respondReverseProxy(jsDevServer)
                return@intercept finish()
            }
        }
    }

    /**
     * Note: to facilitate Javascript development, make sure that any route prefixes used are listed
     * in UstadAppReactProxy
     */
    install(Routing) {
        val di by closestDI()

        prefixRoute(sitePrefix) {
            addHostCheckIntercept()
            personAuthRegisterRoute()
            route("UmAppDatabase") {
                UmAppDatabase_KtorRoute(DoorHttpServerConfig(json = json, logger = NapierDoorLogger())) { call ->
                    di.on(call).direct.instance(tag = DoorTag.TAG_DB)
                }
            }
            SiteRoute()

            GetAppRoute()

            route("config") {
                route("api"){
                    route("learningspaces") {
                        LearningSpaceRoute(
                            createLearningSpaceUseCase = di.direct.instance()
                        )
                    }
                }
            }

            route("api") {
                route("account"){
                    SetPasswordRoute(
                        useCase = { call ->
                            di.on(call).direct.instance()
                        }
                    )
                }

                route("pbkdf2"){
                    Pbkdf2Route()
                }

                route("contentupload") {
                    ContentUploadRoute()
                }

                route("import") {
                    ContentEntryImportRoute()
                }

                route("blob") {
                    BlobUploadServerRoute(
                        useCase = { call ->
                            di.on(call).direct.instance()
                        }
                    )

                    CacheRoute(
                        cache = di.direct.instance()
                    )
                }

                route("content") {
                    ContentEntryVersionRoute(
                        useCase = { call -> di.on(call).direct.instance() }
                    )
                }

                route("contententryimportjob"){
                    ContentEntryImportJobRoute(
                        json = di.direct.instance(),
                        dbFn = { call -> di.on(call).direct.instance(tag = DoorTag.TAG_DB) },
                        cancelImportContentEntryServerUseCase = { call -> di.on(call).direct.instance() }
                    )
                }

                route("person") {
                    route("bulkadd") {
                        BulkAddPersonRoute(
                            enqueueBulkAddPersonServerUseCase = { call -> di.on(call).direct.instance() },
                            bulkAddPersonStatusMap = { call -> di.on(call).direct.instance() },
                            json = json,
                        )
                    }
                }

                /**
                 * Xapi-Ext contains non-standard xapi endpoint services that we use internally;
                 * specfically SaveStatementOnUnload and StartHttpXapiSession
                 */
                route("xapi-ext") {
                    SaveStatementOnUnloadRoute(
                        statementResource = { call -> di.on(call).direct.instance() },
                        json = json,
                    )

                    ResumeOrStartXapiSessionRoute(
                        resumeOrStartXapiSessionUseCase = { call -> di.on(call).direct.instance() },
                        verifyClientUserSessionUseCase  = { call -> di.on(call).direct.instance() },
                        json = json,
                    )
                }

                route("xapi/{pathSegments...}") {
                    XapiRoute(
                        xapiHttpServerUseCase = { call -> di.on(call).direct.instance() }
                    )
                }

                CacheRoute(
                    cache = di.direct.instance()
                )
            }

            static("umapp") {
                resources("umapp")
                static("/") {
                    defaultResource("umapp/index.html")
                }
            }

            staticResources(
                remotePath = "staticfiles",
                basePackage = "staticfiles"
            )

            //Handle default route when running behind proxy
            if(!jsDevServer.isNullOrBlank()) {
                webSocketProxyRoute(jsDevServer)
            }else {
                route("/"){
                    get{
                        call.respondRedirect("umapp/")
                    }
                }
            }
        }
    }

    //Tell anyone looking that the server is up/running and where to find logs
    // As per logback.xml
    val logDir = System.getProperty("logs_dir") ?: "./log/"
    val printableServerUrl = if(dbMode == CONF_DBMODE_VIRTUALHOST) {
        "*:${appConfig.port}"
    }else {
        appConfig.siteUrl()
    }

    println("Ustad server is running on $printableServerUrl . Logging to $logDir .")
    println()
    println("You can connect the Android client to this address as per README.md .")
    println()
    if(jsDevServer != null) {
        println("Javascript development mode is enabled. If you want to use the web client in a browser, you must run: ")
        println("./gradlew app-react:jsRun")
        println("Then open $printableServerUrl in your browser. See app-react/README.md for more details.")
    }else if(this::class.java.getResource("/umapp/index.html") != null) {
        println(" This build includes the web client, you can access it by opening $printableServerUrl in your browser.")
    }else {
        println(" This build does not include the web client and Javascript dev mode is not enabled.")
        println(" If you want to use the web client in a browser, please see app-react/README.md .")
    }
    println()
    println("Use [Ctrl+C] to stop.")
}

