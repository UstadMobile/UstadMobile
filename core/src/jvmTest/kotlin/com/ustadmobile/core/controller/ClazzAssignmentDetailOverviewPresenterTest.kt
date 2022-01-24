
package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorLifecycleObserver

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.mockito.kotlin.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */

class ClazzAssignmentDetailOverviewPresenterTest {

    private lateinit var accountManager: UstadAccountManager
    private lateinit var testClazz: Clazz

    private val loggedInPersonUid:Long = 234568

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzAssignmentDetailOverviewView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

    private lateinit var di: DI

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        val serverUrl = "https://dummysite.ustadmobile.app/"

        accountManager = mock{
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
            on{activeAccount}.thenReturn(UmAccount(loggedInPersonUid,"","",serverUrl))
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
        }

        repo = di.directActiveRepoInstance()

        repoClazzAssignmentDaoSpy = spy(repo.clazzAssignmentDao)
        whenever(repo.clazzAssignmentDao).thenReturn(repoClazzAssignmentDaoSpy)

        testClazz = Clazz("Test clazz").apply {
            clazzStartTime = DateTime(2020, 10, 10).unixMillisLong
            clazzUid = repo.clazzDao.insert(this)
        }


        //TODO: insert any entities required for all tests
    }

    fun createPerson(isAdmin: Boolean) {
        val student = Person().apply {
            admin = isAdmin
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = loggedInPersonUid
            repo.insertPersonOnlyAndGroup(this)
        }

        val clazzEnrolment = ClazzEnrolment().apply {
            clazzEnrolmentPersonUid = student.personUid
            clazzEnrolmentClazzUid = testClazz.clazzUid
            clazzEnrolmentRole = if(isAdmin) ClazzEnrolment.ROLE_TEACHER else ClazzEnrolment.ROLE_STUDENT
            clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

    }

    @Test
    fun givenClazzAssignmentExists_whenOnCreateCalled_thenClazzAssignmentIsSetOnView() {
        createPerson(true)
        val testEntity = ClazzAssignment().apply {
            //set variables here
            caUid = repo.clazzAssignmentDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.caUid, entityValSet.caUid)
    }

    @Test
    fun givenClazzAssignmentExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        createPerson(true)

        val testEntity = ClazzAssignment().apply {
            //set variables here
            caUid = repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        presenter.handleClickEdit()
        val systemImpl: UstadMobileSystemImpl by di.instance()
        verify(systemImpl, timeout(5000)).go(eq(ClazzAssignmentEditView.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())), any())
    }

    @Test
    fun givenClazzAssignmentWithPrivateCommentsEnabled_whenStudentViews_thenShowScoreWithPrivateComments(){
        createPerson(false)

        val testEntity = ClazzAssignment().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caUid = repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        verify(mockView, timeout(1000)).showPrivateComments = eq(true)
        verify(mockView, timeout(1000)).showFileSubmission = eq(true)
        verify(mockView, timeout(1000)).maxNumberOfFilesSubmission = eq(3)


    }

    @Test
    fun givenClazzAssignmentWithPrivateCommentsEnabled_whenTeacherViews_thenDontShowScoreAndPrivateComments(){
        createPerson(true)

        val testEntity = ClazzAssignment().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caUid = repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        verify(mockView, timeout(1000)).showPrivateComments = eq(false)
        verify(mockView, timeout(1000)).showFileSubmission = eq(false)

    }


}
