package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert


class ClazzAssignmentEditPresenterTest : AbstractSetup() {

    lateinit var systemImplSpy: UstadMobileSystemImpl
    private val context = Any()


    @Before
    fun setUp() {
        checkJndiSetup()
        val impl = UstadMobileSystemImpl.instance

        val db = UmAppDatabase.getInstance(Any())

        //do inserts
        insert(db, true)

        //Set active logged in account
        UmAccountManager.setActiveAccount(umAccount!!, Any(), impl)
        systemImplSpy = spy(impl)

    }

    @After
    fun tearDown() {
    }


    fun createMockViewAndPresenter(presenterArgs: Map<String, String> = mapOf())
            : Pair<ClazzAssignmentEditView, ClazzAssignmentEditPresenter> {
        val mockView = mock<ClazzAssignmentEditView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        val mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzAssignmentEditPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenHandleSaveClicked_shouldPresist() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        val newAssignment = ClazzAssignment()
        newAssignment.clazzAssignmentUid = 40L
        newAssignment.clazzAssignmentClazzUid = 42L
        presenter.handleSaveAssignment(newAssignment)

        val newCA = UmAppDatabase.getInstance(context).clazzAssignmentDao.findByUidAsync(40)
        assert(newCA != null)

        verify(view, timeout(1000)).finish()
    }

    @Test
    fun givenPresenterCreated_whenContentAddedAndHandleSaveClicked_shouldPersistJoins(){
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()
        presenter.onCreate(mapOf())

        val newAssignment = ClazzAssignment()
        newAssignment.clazzAssignmentUid = 40L
        newAssignment.clazzAssignmentClazzUid = 42L

        val ce = ContentEntry()
        ce.entryId = "424242"
        ce.title = "testing"
        ce.leaf = true
        ce.contentEntryUid = UmAppDatabase.getInstance(context).contentEntryDao.insert(ce)
        presenter.handleContentEntryAdded(ce)

        presenter.handleSaveAssignment(newAssignment)

        val newCA = UmAppDatabase.getInstance(context).clazzAssignmentDao.findByUidAsync(40)
        assert(newCA != null)

        val caCEJ : List<ClazzAssignmentContentJoin> = UmAppDatabase.getInstance(context).clazzAssignmentContentJoinDao.findJoinsByAssignmentUidList(40)
        assert(caCEJ.isNotEmpty())

        verify(view, timeout(1000)).finish()
    }

    @Test
    fun givenClazzAssignmentWithExistingContentLoaded_whenHandleClickAddContentAndSave_shouldPersist(){
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter()

        val newAssignment = ClazzAssignment()
        newAssignment.clazzAssignmentUid = 40L
        newAssignment.clazzAssignmentClazzUid = 42L

        val ce = ContentEntry()
        ce.entryId = "424242"
        ce.title = "testing"
        ce.leaf = true
        ce.contentEntryUid = UmAppDatabase.getInstance(context).contentEntryDao.insert(ce)

        val ce2 = ContentEntry()
        ce2.entryId = "42424221212121"
        ce2.title = "testing2"
        ce2.leaf = true
        ce2.contentEntryUid = UmAppDatabase.getInstance(context).contentEntryDao.insert(ce2)

        newAssignment.clazzAssignmentUid =
                UmAppDatabase.getInstance(context).clazzAssignmentDao.insert(newAssignment)

        val cej = ClazzAssignmentContentJoin()
        cej.clazzAssignmentContentJoinContentUid = ce.contentEntryUid
        cej.clazzAssignmentContentJoinClazzAssignmentUid = newAssignment.clazzAssignmentUid

        cej.clazzAssignmentContentJoinUid =
                UmAppDatabase.getInstance(context).clazzAssignmentContentJoinDao.insert(cej)

        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "40",
                UstadView.ARG_CLAZZ_UID to "42"))

        //Verify TODO
        verify(view, timeout(1000)).setClazzAssignment(newAssignment)


        presenter.handleContentEntryAdded(ce2)

        presenter.handleSaveAssignment(newAssignment)

        val newCA =
                UmAppDatabase.getInstance(context).clazzAssignmentDao.findByUidAsync(40)
        assert(newCA != null)

        val caCEJ : List<ClazzAssignmentContentJoin> =
                UmAppDatabase.getInstance(context).clazzAssignmentContentJoinDao.findJoinsByAssignmentUidList(40)
        assert(caCEJ.isNotEmpty())

        verify(view, timeout(1000)).finish()


    }
}