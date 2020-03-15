package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.ext.PersonWithClazzandRole
import com.ustadmobile.util.ext.createTeacherRole
import com.ustadmobile.util.ext.grantClazzRoleToPerson
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert


class ClazzAssignmentEditPresenterTest {

    lateinit var systemImplSpy: UstadMobileSystemImpl
    lateinit var mockContext: Any
    lateinit var db : UmAppDatabase
    lateinit var data: PersonWithClazzandRole

    @Before
    fun setUp() {
        checkJndiSetup()
        val impl = UstadMobileSystemImpl.instance

        db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()

        //do inserts
        val teacher = Person("teacher", "Teacher" ,  "One")
        teacher.active = true
        val clazz = Clazz("Class A")
        clazz.isClazzActive = true
        val teacherRole = db.createTeacherRole()
        data = db.grantClazzRoleToPerson(teacher, clazz, teacherRole)

        //Set active logged in account
        val teacherAccount=  UmAccount(data.person.personUid, data.person.username,
                "auth", "endpoint")
        UmAccountManager.setActiveAccount(teacherAccount, Any(), impl)

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
        mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzAssignmentEditPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy, db)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenHandleSaveClicked_shouldPresist() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter(
                mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString())
        )
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))

        val newAssignment = ClazzAssignment()
        presenter.handleSaveAssignment(newAssignment)

        timeout(2000)
        val assignments : List<ClazzAssignment> =
                db.clazzAssignmentDao.findByClazzUidFactorySync(data.clazz.clazzUid)
        var newCA: ClazzAssignment  ? = null
        if(assignments.isNotEmpty()){
            newCA = assignments[0]
        }
        assert(newCA != null)

        verify(view, timeout(1000)).finish()
    }

    @Test
    fun givenPresenterCreated_whenContentAddedAndHandleSaveClicked_shouldPersistJoins(){
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter(
                mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString())
        )
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))

        //Add the content
        val ce = ContentEntry()
        ce.entryId = "424242"
        ce.title = "testing"
        ce.leaf = true
        ce.contentEntryUid = db.contentEntryDao.insert(ce)

        //Create a new blank assignment for editing
        val newAssignment = ClazzAssignment()

        presenter.handleContentEntryAdded(ce)

        presenter.handleSaveAssignment(newAssignment)

        timeout(2000)
        val assignments : List<ClazzAssignment> =
                db.clazzAssignmentDao.findByClazzUidFactorySync(data.clazz.clazzUid)
        var newCA: ClazzAssignment  ? = null
        if(assignments.isNotEmpty()) {
            newCA = assignments[0]
        }
        assert(newCA != null)

        val caCEJ : List<ClazzAssignmentContentJoin> =
                db.clazzAssignmentContentJoinDao.findJoinsByAssignmentUidList(
                        newCA!!.clazzAssignmentUid)
        assert(caCEJ.isNotEmpty())

        verify(view, timeout(1000)).finish()
    }

    @Test
    fun givenClazzAssignmentWithExistingContentLoaded_whenHandleClickAddContentAndSave_shouldPersist(){
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter(
                mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString(),
                        UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "40"
                )
        )

        val newAssignment = ClazzAssignment()
        newAssignment.clazzAssignmentClazzUid = data.clazz.clazzUid

        val ce = ContentEntry()
        ce.entryId = "424242"
        ce.title = "testing"
        ce.leaf = true
        ce.contentEntryUid = db.contentEntryDao.insert(ce)

        val ce2 = ContentEntry()
        ce2.entryId = "42424221212121"
        ce2.title = "testing2"
        ce2.leaf = true
        ce2.contentEntryUid = db.contentEntryDao.insert(ce2)

        newAssignment.clazzAssignmentUid =
                db.clazzAssignmentDao.insert(newAssignment)

        val cej = ClazzAssignmentContentJoin()
        cej.clazzAssignmentContentJoinContentUid = ce.contentEntryUid
        cej.clazzAssignmentContentJoinClazzAssignmentUid = newAssignment.clazzAssignmentUid

        cej.clazzAssignmentContentJoinUid =
                db.clazzAssignmentContentJoinDao.insert(cej)

        presenter.onCreate(
                mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to newAssignment.clazzAssignmentUid.toString(),
                UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))

        //Verify TODO
        verify(view, timeout(1000)).setClazzAssignment(any())

        presenter.handleContentEntryAdded(ce2)

        presenter.handleSaveAssignment(newAssignment)

        val newCA = db.clazzAssignmentDao.findByUidAsync(newAssignment.clazzAssignmentUid)
        assert(newCA != null)

        val caCEJ : List<ClazzAssignmentContentJoin> =
                db.clazzAssignmentContentJoinDao.findJoinsByAssignmentUidList(newAssignment.clazzAssignmentUid)
        assert(caCEJ.isNotEmpty())

        verify(view, timeout(1000)).finish()


    }
}