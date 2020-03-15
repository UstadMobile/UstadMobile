package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailAssignmentView
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.ext.PersonWithClazzandRole
import com.ustadmobile.util.ext.createTeacherRole
import com.ustadmobile.util.ext.grantClazzRoleToPerson
import com.ustadmobile.util.test.AbstractSetup
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert


class ClazzAssignmentDetailPresenterTest : AbstractSetup() {

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
            : Pair<ClazzAssignmentDetailView, ClazzAssignmentDetailPresenter> {
        val mockView = mock<ClazzAssignmentDetailView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzAssignmentDetailPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy, db)
        return Pair(mockView, presenter)
    }


    @Test
    fun givenPresenterCreated_whenLoaded_shouldSetUpTabs() {

        //Create assignment
        val assignment : ClazzAssignment = ClazzAssignment()
        assignment.clazzAssignmentClazzUid = data.clazz.clazzUid
        assignment.clazzAssignmentUid = db.clazzAssignmentDao.insert(assignment)

        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter(
                mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString(),
                        UstadView.ARG_CLAZZ_ASSIGNMENT_UID to assignment.clazzAssignmentUid.toString())
        )
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString(),
                UstadView.ARG_CLAZZ_ASSIGNMENT_UID to assignment.clazzAssignmentUid.toString()))

        verify(view, timeout(1000)).setClazzAssignment(any())
        verify(view, timeout(1000)).setupTabs(mutableListOf(
                ClazzAssignmentDetailAssignmentView.VIEW_NAME,
                ClazzAssignmentDetailProgressView.VIEW_NAME))

    }

}