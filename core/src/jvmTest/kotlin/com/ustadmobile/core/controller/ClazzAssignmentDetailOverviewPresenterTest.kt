
package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeSubmitFileSubmissionStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.onActiveAccount
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.core.view.SelectFileView.Companion.ARG_SELECTION_MODE
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
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

    private lateinit var xapiStatementEndpointImpl: XapiStatementEndpoint

    @Before
    fun setup() {
        mockView = mock {
            on { runOnUiThread(any())}.doAnswer{
                Thread(it.getArgument<Any>(0) as Runnable).start()
            }
        }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        xapiStatementEndpointImpl = mock{}

        val serverUrl = "https://dummysite.ustadmobile.app/"

        accountManager = mock{
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
            on{activeAccount}.thenReturn(UmAccount(loggedInPersonUid,"","",serverUrl))
        }


        di = DI {
            import(ustadTestRule.diModule)
            bind<XapiStatementEndpoint>() with scoped(ustadTestRule.endpointScope).singleton {
                xapiStatementEndpointImpl
            }
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
    fun givenClazzAssignment_whenStudentViews_thenShowScoreWithPrivateComments(){
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

        verify(mockView, timeout(1000)).showFileSubmission = eq(true)
        verify(mockView, timeout(1000)).maxNumberOfFilesSubmission = eq(3)
        verify(mockView, timeout(1000)).hasPassedDeadline = eq(false)
        verify(mockView, timeout(5000).atLeastOnce()).clazzAssignmentFileSubmission = any()
        verify(mockView, timeout(1000).atLeastOnce()).fileSubmissionScore = any()
        verify(mockView, timeout(5000).atLeastOnce()).clazzMetrics = any()
        verify(mockView, timeout(1000)).showPrivateComments = eq(true)
        verify(mockView, timeout(1000).times(0)).clazzAssignmentClazzComments

    }

    @Test
    fun givenClazzAssignment_whenTeacherViews_thenDontShowScoreAndPrivateComments(){
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
        verify(mockView, timeout(1000).times(0)).maxNumberOfFilesSubmission
        verify(mockView, timeout(1000).times(0)).hasPassedDeadline
        verify(mockView, timeout(1000).times(0)).clazzMetrics
        verify(mockView, timeout(1000).times(0)).clazzAssignmentFileSubmission
        verify(mockView, timeout(1000).times(0)).fileSubmissionScore
        verify(mockView, timeout(1000).times(0)).clazzAssignmentClazzComments

    }

    @Test
    fun givenUserClicksAddFile_whenClicked_thenGoToSelectFileView(){

        createPerson(false)

        val testEntity = ClazzAssignment().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caFileType = ClazzAssignment.FILE_TYPE_VIDEO
            caUid = repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        mockView.captureLastEntityValue()

        presenter.handleAddFileClicked()

        val testNavController: UstadNavController by di.instance()
        verify(testNavController).navigate(SelectFileView.VIEW_NAME,
                mapOf(ARG_SELECTION_MODE to SelectFileView.SELECTION_MODE_VIDEO))

    }

    @Test
    fun givenUserClicksSubmitButton_whenClicked_thenShouldCreateStatement(){

        createPerson(false)
        val testEntity = ClazzAssignment().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caFileType = ClazzAssignment.FILE_TYPE_VIDEO
            caUid = repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        whenever(mockView.entity).thenReturn(testEntity)

        mockView.captureLastEntityValue()

        val fileSubmissionDaoSpy = spy(repo.assignmentFileSubmissionDao)
        doReturn(fileSubmissionDaoSpy).`when`(repo).assignmentFileSubmissionDao

        presenter.handleSubmitButtonClicked()

        verifyBlocking(fileSubmissionDaoSpy, timeout(1000)){
            setFilesAsSubmittedForStudent(eq(testEntity.caUid),
                any(), eq(true), any())
        }

        runBlocking {
            verify(xapiStatementEndpointImpl, timeout(1000))
                    .storeStatements(any(),
                            any(), any(), any())
        }


    }







}
