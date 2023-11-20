package com.ustadmobile.port.desktop

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.ext.migrationList
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderJvm
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File
import java.util.Locale
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.kodein.di.on

const val TAG_APP_HOME = "AppHome"

const val TAG_DATA_DIR = "DataDir"


@OptIn(ExperimentalXmlUtilApi::class)
val DesktopDiModule = DI.Module("Desktop-Main") {
    bind<SupportedLanguagesConfig>() with singleton {
        SupportedLanguagesConfig()
    }

    bind<StringProvider>() with singleton { StringProviderJvm(Locale.getDefault()) }

    bind<UstadAccountManager>() with singleton {
        UstadAccountManager(instance(), di)
    }

    bind<ApiUrlConfig>() with singleton {
        ApiUrlConfig(presetApiUrl = null)
    }

    bind<File>(tag = TAG_APP_HOME) with singleton {
        System.getProperty("app_home")?.let { File(it) } ?: File(System.getProperty("user.dir"))
    }

    bind<File>(tag = TAG_DATA_DIR) with singleton {
        File(instance<File>(tag = TAG_APP_HOME), "data")
    }

    bind<File>(tag = DiTag.TAG_CONTEXT_DATA_ROOT) with scoped(EndpointScope.Default).singleton {
        val dataDir: File = instance(tag = TAG_DATA_DIR)
        File(dataDir, sanitizeDbNameFromUrl(context.url)).also {
            it.takeIf { !it.exists() }?.mkdirs()
        }
    }

    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(
            dataRoot = instance(tag = TAG_DATA_DIR)
        )
    }

    bind<AuthManager>() with scoped(EndpointScope.Default).singleton {
        AuthManager(context, di)
    }

    bind<Pbkdf2Params>() with singleton {
        val numIterations = UstadMobileConstants.PBKDF2_ITERATIONS
        val keyLength = UstadMobileConstants.PBKDF2_KEYLENGTH

        Pbkdf2Params(numIterations, keyLength)
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
        val contextDataDir: File = on(context).instance(tag = DiTag.TAG_CONTEXT_DATA_ROOT)
        val dbUrl = "jdbc:sqlite:${contextDataDir.absolutePath}/UmAppDatabase.db"
        val nodeIdAndAuth: NodeIdAndAuth = instance()

        DatabaseBuilder.databaseBuilder(UmAppDatabase::class, dbUrl, nodeIdAndAuth.nodeId)
            .addSyncCallback(nodeIdAndAuth)
            .addCallback(ContentJobItemTriggersCallback())
            .addMigrations(*migrationList().toTypedArray())
            .build()
    }

    bind<NodeIdAndAuth>() with scoped(EndpointScope.Default).singleton {
        val systemImpl: UstadMobileSystemImpl = instance()
        val contextIdentifier = sanitizeDbNameFromUrl(context.url)
        systemImpl.getOrGenerateNodeIdAndAuth(contextIdentifier, Any())
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

    bind<XhtmlFixer>() with singleton {
        XhtmlFixerJsoup(xml = instance())
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
        val nodeIdAndAuth: NodeIdAndAuth = instance()
        val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
        db.asRepository(
            RepositoryConfig.repositoryConfig(
                context = Any(),
                endpoint = "${context.url}UmAppDatabase/",
                nodeId = nodeIdAndAuth.nodeId,
                auth = nodeIdAndAuth.auth,
                httpClient = instance(),
                okHttpClient = instance(),
                json = instance()
            )
        )
    }
}