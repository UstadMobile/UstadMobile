
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
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import junit.framework.Assert
import junit.framework.Assert.*
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

    }

    private fun createEntries(nonLeafs: MutableList<Int> = mutableListOf()){
        createdEntries = runBlocking {
            clientDbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(
                    6,parentEntryUid, nonLeafIndexes = nonLeafs)
        }
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        createEntries()
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
        createEntries()
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

    @Test
    fun givenPresenterCreatedInPickerMode_whenOnClickEntryCalledOnALeaf_thenShouldFinishWithResult() {
        createEntries()
        val args = presenterArgs.plus(UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())
        val presenter = ContentEntryList2Presenter(context,
                args , mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        createdEntries?.get(0)?.let { presenter.handleClickEntry(it) }

        argumentCaptor<List<ContentEntry>>().apply{
            verify(mockView, after(defaultTimeout).times(1)).finishWithResult(capture())
            assertEquals("Got expected result", firstValue[0], createdEntries?.get(0))
        }
    }

    @Test
    fun givenPresenterCreatedInPickerMode_whenOnClickEntryCalledOnAFolder_thenShouldOpenIt(){
        createEntries(mutableListOf(0))
        runBlocking {
            clientDbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(
                    6, createdEntries?.get(0)?.contentEntryUid!!)
        }
        val args = presenterArgs.plus(UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())
        val presenter = ContentEntryList2Presenter(context,
                args , mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()
        createdEntries?.get(0)?.let { presenter.handleClickEntry(it) }

        argumentCaptor<Long>().apply{
            verify(repoContentEntrySpyDao, timeout(defaultTimeout).times(2)).getChildrenByParentUidWithCategoryFilterOrderByNameAsc(
                    capture(), eq(0), eq(0), eq(clientDbRule.account.personUid))
            assertEquals("Expected folder was opened", secondValue, createdEntries?.get(0)?.contentEntryUid)
        }
    }


    @Test
    fun givenPresenterCreatedInPickerMode_whenOnClickEntryCalledOnAFolderForEntrySelection_thenShouldOpenItAndFinishWithResultWhenSelected(){
        createEntries(mutableListOf(0))
        val createdChildEntries = runBlocking {
            clientDbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(
                    6, createdEntries?.get(0)?.contentEntryUid!!)
        }
        val args = presenterArgs.plus(UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())
        val presenter = ContentEntryList2Presenter(context,
                args , mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        createdEntries?.get(0)?.let { presenter.handleClickEntry(it) }

        mockView.waitForListToBeSet()

        createdChildEntries[0].let { presenter.handleClickEntry(it) }

        argumentCaptor<List<ContentEntry>>().apply{
            verify(mockView, after(defaultTimeout).times(1)).finishWithResult(capture())
            assertEquals("Got expected result", firstValue[0], createdChildEntries[0])
        }
    }


    @Test
    fun givenPresenterCreatedInPickerMode_whenOnBackPressedWhileInAFolder_thenShouldGoBackToThePreviousParentEntry(){
        createEntries(mutableListOf(0))
        val createdChildEntries = runBlocking {
            clientDbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(
                    6, createdEntries?.get(0)?.contentEntryUid!!)
        }
        val args = presenterArgs.plus(UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())
        val presenter = ContentEntryList2Presenter(context,
                args , mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        createdEntries?.get(0)?.let { presenter.handleClickEntry(it) }

        mockView.waitForListToBeSet()

        val canGoBack = createdChildEntries[0].let { presenter.handleOnBackPressed() }

        assertTrue("Can go back to the previous folder", canGoBack)

        argumentCaptor<Long>().apply{
            verify(repoContentEntrySpyDao, timeout(defaultTimeout).times(3)).getChildrenByParentUidWithCategoryFilterOrderByNameAsc(
                    capture(), eq(0), eq(0), eq(clientDbRule.account.personUid))
            assertEquals("Went back to the expected folder", thirdValue, parentEntryUid)
        }
    }


    @Test
    fun givenPresenterCreatedInPickerMode_whenOnBackPressedWhileOnATopParentFolder_thenShouldNotGoBack() {
        createEntries()
        val args = presenterArgs.plus(UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())
        val presenter = ContentEntryList2Presenter(context,
                args , mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        val canGoBack = createdEntries?.get(0).let { presenter.handleOnBackPressed() }

        assertFalse("Can not go back to the previous folder", canGoBack)
    }
}