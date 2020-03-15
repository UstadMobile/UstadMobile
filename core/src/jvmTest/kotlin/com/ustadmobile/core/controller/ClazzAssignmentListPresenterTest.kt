package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.ext.PersonWithClazzandRole
import com.ustadmobile.util.ext.createTeacherRole
import com.ustadmobile.util.ext.grantClazzRoleToPerson
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.After
import org.junit.Before
import org.junit.Test


class ClazzAssignmentListPresenterTest() {

    lateinit var systemImplSpy: UstadMobileSystemImpl
    lateinit var db : UmAppDatabase
    lateinit var data: PersonWithClazzandRole
    lateinit var mockContext: Any

    @Before
    fun setUp() {
        checkJndiSetup()
        val impl = UstadMobileSystemImpl.instance

        db = UmAppDatabase.getInstance(Any())

        db.clearAllTables()

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
            : Pair<ClazzAssignmentListView, ClazzAssignmentListPresenter> {
        val mockView = mock<ClazzAssignmentListView> {
            on { runOnUiThread(any()) }.doAnswer {
                Thread(it.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }
        mockContext = mock<DoorLifecycleOwner> {}
        val presenter = ClazzAssignmentListPresenter(mockContext,
                presenterArgs, mockView, systemImplSpy, db)
        return Pair(mockView, presenter)
    }

    @Test
    fun givenPresenterCreated_whenClickAssignment_shouldCallGoToDetail() {
        // create presenter, with a mock view, check that it makes that call
        val (_, presenter) = createMockViewAndPresenter(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))

        val ca = ClazzAssignmentWithMetrics()
        ca.clazzAssignmentUid = 42

        presenter.handleClickAssignment(ca)

        verify(systemImplSpy, timeout(1000)).go(ClazzAssignmentDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "42",
                        UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()), mockContext)
    }

    @Test
    fun givenPresenterCreated_whenClickNewAssignment_shouldCallGoToDetail() {
        // create presenter, with a mock view, check that it makes that call
        val (_, presenter) = createMockViewAndPresenter(mapOf(UstadView.ARG_CLAZZ_UID to
                data.clazz.clazzUid.toString()))
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))

        presenter.handleClickNewAssignment()

        verify(systemImplSpy, timeout(1000)).go(ClazzAssignmentEditView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to "0",
                        UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()), mockContext)

    }

    @Test
    fun givenPresenterCreated_whenOnCreated_setSetView() {
        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter(mapOf(UstadView.ARG_CLAZZ_UID to
                data.clazz.clazzUid.toString()))
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))

        //Check against admin
        verify(view, timeout(2000)).setEditVisibility(eq(true))
        verify(view, timeout(2000)).setListProvider(any())

    }

    @Test
    fun givenPresenterCreated_whenOnCreatedWithoutPermission_setSetView() {

        //Set another account
        val randomPerson = Person("random", "Random", "Person",
                true)
        randomPerson.personUid = db.personDao.insert(randomPerson)
        val randomAccount = UmAccount(randomPerson.personUid, "random",
                "auth", "endpoint")
        UmAccountManager.setActiveAccount(randomAccount, Any(), UstadMobileSystemImpl.instance)

        // create presenter, with a mock view, check that it makes that call
        val (view, presenter) = createMockViewAndPresenter(mapOf(UstadView.ARG_CLAZZ_UID to
                data.clazz.clazzUid.toString()))
        presenter.onCreate(mapOf(UstadView.ARG_CLAZZ_UID to data.clazz.clazzUid.toString()))

        verify(view, timeout(1000)).setEditVisibility(eq(false))

    }

}