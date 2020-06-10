
package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContentEntryList2PresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ContentEntryList2View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoContentEntrySpyDao: ContentEntryDao

    private val defaultTimeout = 3000L

    private val parentEntryUid = 100001L

    private var createdEntries: List<ContentEntry>? = null

    val presenterArgs = mapOf(ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT,
            ARG_PARENT_ENTRY_UID to parentEntryUid.toString())

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoContentEntrySpyDao = spy(clientDbRule.db.contentEntryDao)
        whenever(clientDbRule.db.contentEntryDao).thenReturn(repoContentEntrySpyDao)
        createdEntries = runBlocking {
            clientDbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(
                    6,parentEntryUid)
        }

    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val presenter = ContentEntryList2Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        verify(repoContentEntrySpyDao, timeout(defaultTimeout)).getChildrenByParentUidWithCategoryFilterOrderByNameAsc(
                eq(parentEntryUid), eq(0), eq(0), eq(clientDbRule.account.personUid))

        verify(mockView, timeout(defaultTimeout)).list = any()
    }


    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenter = ContentEntryList2Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        createdEntries?.get(0)?.let { presenter.handleClickEntry(it) }

        verify(systemImplRule.systemImpl, timeout(defaultTimeout)).go(eq(ContentEntry2DetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to createdEntries?.get(0)?.contentEntryUid.toString())), any())
    }


}