package com.ustadmobile.core.controller

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryProgressDao
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.util.test.ext.insertVideoContent
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import java.io.IOException
import javax.naming.InitialContext

class VideoContentPresenterTest {

    lateinit var accountManager: UstadAccountManager

    lateinit var di: DI

    private var context: Any = Any()

    private lateinit var mockView: VideoPlayerView

    private lateinit var mockEndpoint: XapiStatementEndpoint

    var container: Container? = null

    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()

        mockView = mock { }
        mockEndpoint = mock {}
        val systemImplSpy = spy(UstadMobileSystemImpl.instance)
        val endpointScope = EndpointScope()
        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { systemImplSpy!! }
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
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope!!).singleton {
                spy(instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).asRepository<UmAppDatabase>(Any(), context.url, "", defaultHttpClient(), null))
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
        }


    }

    @Test
    fun givenVideo_whenVideoStartsPlaying_ProgressIsUpdatedAndStatementSent() {

        val presenterArgs = mapOf(UstadView.ARG_CONTENT_ENTRY_UID to
                container!!.containerContentEntryUid.toString(),
                UstadView.ARG_CONTAINER_UID to container!!.containerUid.toString())
        
        val presenter = VideoContentPresenter(context,
                presenterArgs, mockView, di)
        presenter.onCreate(null)

        presenter.updateProgress(0, 100, true)

        verify(mockEndpoint).storeStatements(any(), eq(""), eq(container!!.containerContentEntryUid))

    }


}