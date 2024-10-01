package com.ustadmobile.lib.rest

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.random.Random
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.lib.rest.ext.insertDefaultSite
import java.util.Locale
import java.util.Properties

/**
 * Creates a KodeIn DI Module that will contain most of what the test application engine needs to run
 */
fun commonTestKtorDiModule(
    endpointScope: LearningSpaceScope
) = DI.Module("Common Ktor Test Module") {
    bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
        NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
    }

    bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
        val nodeIdAndAuth : NodeIdAndAuth = instance()
        DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
            "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite", nodeId = nodeIdAndAuth.nodeId)
            .build().also { db ->
                db.clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
                db.insertDefaultSite()
            }
    }

    bind<UmAppDataLayer>() with scoped(endpointScope).singleton {
        UmAppDataLayer(localDb = instance(tag = DoorTag.TAG_DB), repository = null)
    }

    bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
        XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
        }
    }

    bind<Settings>() with singleton {
        PropertiesSettings(
            delegate = Properties(),
            onModify = {
                //do nothing
            }
        )
    }

    bind<UstadMobileSystemImpl>() with singleton {
        UstadMobileSystemImpl(settings = instance(), langConfig = instance())
    }

    bind<SupportedLanguagesConfig>() with singleton {
        SupportedLanguagesConfig(
            systemLocales = listOf(Locale.getDefault().country),
            settings = instance()
        )
    }

    bind<Pbkdf2Params>() with singleton {
        Pbkdf2Params()
    }

    bind<AuthManager>() with scoped(endpointScope).singleton {
        AuthManager(context, di)
    }

}