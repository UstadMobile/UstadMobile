package com.ustadmobile.core.controller

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.mockito.kotlin.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.ext.insertVideoContent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import java.io.IOException
import javax.naming.InitialContext

class VideoContentPresenterTest {

    lateinit var accountManager: UstadAccountManager

    lateinit var di: DI

    private var context: Any = Any()

    private lateinit var mockView: VideoPlayerView

    private lateinit var mockEndpoint: XapiStatementEndpoint

    var container: Container? = null

    var selectedClazzUid = 10001L

    @Before
    @Throws(IOException::class)
    fun setup() {
        mockView = mock { }
        mockEndpoint = mock {}
        val endpointScope = EndpointScope()
        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { spy(UstadMobileSystemImpl()) }
            bind<UstadAccountManager>() with singleton { UstadAccountManager(instance(), Any(), di) }
            bind<Gson>() with singleton {
                val builder = GsonBuilder()
                builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
                builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
                builder.registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
                builder.create()
            }
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope!!).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                    it.preload()
                })
            }

            bind<HttpClient>() with singleton {
                HttpClient(OkHttp) {
                    install(JsonFeature)
                    install(HttpTimeout)
                    engine {
                        preconfigured = instance()
                    }
                }
            }

            bind<OkHttpClient>() with singleton {
                OkHttpClient()
            }

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope!!).singleton {
                spy(instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).asRepository(
                    repositoryConfig(Any(), context.url, instance(), instance())))
            }
            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

            bind<XapiStatementEndpoint>() with scoped(endpointScope).singleton {
                mockEndpoint
            }
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        accountManager = di.direct.instance()
        accountManager.activeAccount.personUid = 42

        runBlocking {
            container = repo.insertVideoContent()
            Clazz().apply{
                this.clazzUid = selectedClazzUid
                repo.clazzDao.insert(this)
            }
        }


    }

    @Test
    fun givenVideo_whenVideoStartsPlaying_ProgressIsUpdatedAndStatementSent() {

        val presenterArgs = mapOf(UstadView.ARG_CONTENT_ENTRY_UID to
                container!!.containerContentEntryUid.toString(),
                UstadView.ARG_CLAZZUID to selectedClazzUid.toString(),
                UstadView.ARG_CONTAINER_UID to container!!.containerUid.toString())
        
        val presenter = VideoContentPresenter(context,
                presenterArgs, mockView, di)
        presenter.onCreate(null)

        presenter.updateProgress(0, 100, true)

        verify(mockEndpoint, timeout(5000)).storeStatements(any(),
                eq(""), eq(container!!.containerContentEntryUid), eq(selectedClazzUid))

    }


}