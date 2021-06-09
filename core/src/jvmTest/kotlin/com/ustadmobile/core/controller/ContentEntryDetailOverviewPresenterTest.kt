
package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.util.getSystemTimeInMillis
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.lang.Thread.sleep

class ContentEntryDetailOverviewPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ContentEntryDetailOverviewView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoContentEntrySpyDao: ContentEntryDao

    private var createdEntry: ContentEntry? = null

    private lateinit var entryContainer: Container

    private val defaultTimeout = 3000L

    private lateinit var containerManager: ContainerDownloadManager

    private var presenterArgs: Map<String, String>? = null

    private lateinit var localAvailabilityManager: LocalAvailabilityManager

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        containerManager = spy{}
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        localAvailabilityManager = mock {  }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
            bind<LocalAvailabilityManager>() with singleton { localAvailabilityManager }
        }

        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        repoContentEntrySpyDao = spy(repo.contentEntryDao)
        whenever(repo.contentEntryDao).thenReturn(repoContentEntrySpyDao)
        createdEntry = ContentEntry().apply {
            title = "Dummy Entry"
            leaf = true
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        entryContainer = Container().apply {
            containerContentEntryUid = createdEntry?.contentEntryUid ?: 0L
            cntLastModified = getSystemTimeInMillis()
            containerUid = repo.containerDao.insert(this)
        }

        presenterArgs = mapOf(ARG_ENTITY_UID to createdEntry?.contentEntryUid.toString())
    }

    @Test
    fun givenContentEntryExists_whenLaunched_thenShouldShowContentEntryAndMonitorAvailability(){
        val presenter = ContentEntryDetailOverviewPresenter(context,
                presenterArgs!!, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        nullableArgumentCaptor<ContentEntryWithMostRecentContainer>().apply {
            verify(mockView, timeout(defaultTimeout).atLeastOnce()).entity = capture()
            Assert.assertEquals("Expected entry was set on view",
                    createdEntry?.contentEntryUid, lastValue!!.contentEntryUid)
        }

        presenter.onStart()

        verify(localAvailabilityManager, timeout(5000)).addMonitoringRequest(argWhere {
            entryContainer.containerUid in it.containerUidsToMonitor
        })
    }


    @Test
    fun givenContentEntryExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled(){
        val presenter = ContentEntryDetailOverviewPresenter(context,
                presenterArgs!!, mockView, di, mockLifecycleOwner)
        val systemImpl: UstadMobileSystemImpl by di.instance()

        presenter.onCreate(null)

        sleep(defaultTimeout)

        presenter.handleClickEdit()

        verify(systemImpl).go(eq(ContentEntryEdit2View.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to createdEntry?.contentEntryUid.toString(),
                        UstadView.ARG_LEAF to true.toString())), any())
    }

}