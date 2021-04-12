package com.ustadmobile.core.util

import com.google.gson.Gson
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import javax.naming.InitialContext

fun DI.onActiveAccount(): DI {
    val accountManager: UstadAccountManager by instance()
    return on(accountManager.activeAccount)
}

fun DI.onActiveAccountDirect() = direct.on(direct.instance<UstadAccountManager>().activeAccount)

fun DI.activeDbInstance() = onActiveAccount().instance<UmAppDatabase>(tag = TAG_DB)

fun DI.activeRepoInstance() = onActiveAccount().instance<UmAppDatabase>(tag = TAG_REPO)

fun DI.directActiveDbInstance() = onActiveAccountDirect().instance<UmAppDatabase>(tag = TAG_DB)

fun DI.directActiveRepoInstance() = onActiveAccountDirect().instance<UmAppDatabase>(tag = TAG_REPO)

/**
 * UstadTestRule makes a fresh almost-ready-to-go DI module for each test run. The DB and SystemImpl
 * are wrapped with spy, so they can be used to in verify calls.
 *
 * Simply override the built in bindings if required for specific tests
 */
class UstadTestRule: TestWatcher() {

    lateinit var endpointScope: EndpointScope

    private var systemImplSpy: UstadMobileSystemImpl? = null

    lateinit var diModule: DI.Module

    lateinit var httpClient: HttpClient

    override fun starting(description: Description?) {
        endpointScope = EndpointScope()
        systemImplSpy = spy(UstadMobileSystemImpl.instance)
        httpClient = HttpClient(OkHttp) {
            install(JsonFeature)
            install(HttpTimeout)
        }

        diModule = DI.Module("UstadTestRule") {
            bind<UstadMobileSystemImpl>() with singleton { systemImplSpy!! }
            bind<UstadAccountManager>() with singleton { UstadAccountManager(instance(), Any(), di) }
            bind<UmAppDatabase>(tag = TAG_DB) with scoped(endpointScope!!).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                })
            }

            bind<UmAppDatabase>(tag = TAG_REPO) with scoped(endpointScope!!).singleton {
                spy(instance<UmAppDatabase>(tag = TAG_DB).asRepository<UmAppDatabase>(Any(), context.url, "",
                    instance(), null))
            }

            bind<NetworkManagerBle>() with singleton { mock<NetworkManagerBle> { } }

            bind<ContainerMounter>() with singleton { EmbeddedHTTPD(0, di).also { it.start() } }

            bind<Gson>() with singleton {
                Gson()
            }

            bind<HttpClient>() with singleton {
                httpClient
            }

            bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
                XmlPullParserFactory.newInstance().also {
                    it.isNamespaceAware = true
                }
            }

            bind<XmlPullParserFactory>(tag = DiTag.XPP_FACTORY_NSUNAWARE) with singleton {
                XmlPullParserFactory.newInstance()
            }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
        }
    }

    override fun finished(description: Description?) {
        UstadMobileSystemImpl.instance.clearPrefs()
        httpClient.close()
    }

}