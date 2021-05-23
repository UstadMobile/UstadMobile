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
import com.ustadmobile.core.contentformats.xapi.endpoints.storeProgressStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
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
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import org.mockito.ArgumentMatchers
import java.io.IOException
import javax.naming.InitialContext

class VideoContentPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    lateinit var accountManager: UstadAccountManager

    val account = UmAccount(42, "username", "fefe1010fe",
            "http://localhost/")

    lateinit var di: DI

    private var context: Any = Any()

    private lateinit var mockView: VideoPlayerView

    private lateinit var mockEndpoint: XapiStatementEndpoint

    var container: Container? = null

    private lateinit var endpoint: Endpoint

    @Before
    fun setup() {
        val endpointUrl = account.endpointUrl!!
        endpoint = Endpoint(endpointUrl)

        mockEndpoint = mock {}
        di = DI {
            import(ustadTestRule.diModule)
            bind<XapiStatementEndpoint>() with singleton { mockEndpoint }
        }

        di.direct.instance<UstadAccountManager>().activeAccount = account

        mockView = mock{
            on { runOnUiThread(any())}.doAnswer{
                Thread(it.getArgument<Any>(0) as Runnable).start()
            }
        }

        val db: UmAppDatabase by di.on(endpoint).instance(tag = UmAppDatabase.TAG_DB)
        val repo: UmAppDatabase by di.on(endpoint).instance(tag = UmAppDatabase.TAG_REPO)

        runBlocking {
            container = repo.insertVideoContent()

            val entry = db.contentEntryDao.findByUidAsync(container!!.containerContentEntryUid)
        }

    }

    @Test
    fun givenVideo_whenVideoStartsPlaying_ProgressIsUpdatedAndStatementSent() {
        Assert.assertNotNull(container)
        val presenterArgs = mapOf(UstadView.ARG_CONTENT_ENTRY_UID to
                container!!.containerContentEntryUid.toString(),
                UstadView.ARG_CONTAINER_UID to container!!.containerUid.toString())


        val presenter = VideoContentPresenter(context,
                presenterArgs, mockView, di)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).entry = any()

        presenter.updateProgress(0, 100, true)

        runBlocking {
            verify(mockEndpoint, timeout(5000)).storeStatements(
                    any(), any(), any())
        }

    }


}