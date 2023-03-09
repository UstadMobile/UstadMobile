package com.ustadmobile.core.controller

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.util.test.ext.insertVideoContent
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.mockito.kotlin.*

class VideoContentPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    lateinit var accountManager: UstadAccountManager

    lateinit var di: DI

    private var context: Any = Any()

    private lateinit var mockView: VideoContentView

    private lateinit var mockEndpoint: XapiStatementEndpoint

    var container: Container? = null

    var selectedClazzUid = 10001L

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        mockEndpoint = mock {}
        val endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
            bind<XapiStatementEndpoint>() with scoped(endpointScope).singleton {
                mockEndpoint
            }
        }

        mockView = mock{ }


        val repo: UmAppDatabase by di.activeRepoInstance()

        accountManager = di.direct.instance()
        val userPerson = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                personUid = 42
                firstNames = "Jane"
                lastName = "Doe"
                username = "janedoe"
            })
        }

        accountManager.startLocalTestSessionBlocking(userPerson, accountManager.activeEndpoint.url)


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
        Assert.assertNotNull(container)
        val presenterArgs = mapOf(UstadView.ARG_CONTENT_ENTRY_UID to
                container!!.containerContentEntryUid.toString(),
                UstadView.ARG_CLAZZUID to selectedClazzUid.toString(),
                UstadView.ARG_CONTAINER_UID to container!!.containerUid.toString())


        val presenter = VideoContentPresenter(context,
                presenterArgs, mockView, di)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).entry = any()

        presenter.updateProgress(0, 100, true)

        runBlocking {
            verify(mockEndpoint, timeout(5000)).storeStatements(any(),
                    any(), eq(container!!.containerContentEntryUid), eq(selectedClazzUid))
        }

    }


}