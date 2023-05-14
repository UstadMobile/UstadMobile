
package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.SelectFileView
import com.ustadmobile.core.view.SelectFileView.Companion.ARG_MIMETYPE_SELECTED
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
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

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

    private lateinit var di: DI

    private lateinit var repo: UmAppDatabase

    private lateinit var xapiStatementEndpointImpl: XapiStatementEndpoint

    private lateinit var mockNavController: UstadNavController

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
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
        mockNavController = di.direct.instance()


        repoClazzAssignmentDaoSpy = spy(repo.clazzAssignmentDao)
        whenever(repo.clazzAssignmentDao).thenReturn(repoClazzAssignmentDaoSpy)

        testClazz = Clazz("Test clazz").apply {
            clazzStartTime = DateTime(2020, 10, 10).unixMillisLong
            clazzUid = repo.clazzDao.insert(this)
        }


        //TODO: insert any entities required for all tests
    }

    fun createPerson(role: Int) {
        val student = Person().apply {
            admin = false
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = loggedInPersonUid
            repo.insertPersonOnlyAndGroup(this)
        }

        val clazzEnrolment = ClazzEnrolment().apply {
            clazzEnrolmentPersonUid = student.personUid
            clazzEnrolmentClazzUid = testClazz.clazzUid
            clazzEnrolmentRole = role
            clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

    }


    //@Test
    fun givenUserNotAssignedInGroup_whenShown_displayErrorAndDontShowSubmitAndAddTextFileButtonsAndPrivateOff(){
        createPerson(ClazzEnrolment.ROLE_STUDENT)

        val group = CourseGroupSet().apply {
            cgsName = "Test Group"
            cgsTotalGroups = 2
            cgsClazzUid = testClazz.clazzUid
            cgsUid = repo.courseGroupSetDao.insert(this)
        }

        val testEntity = ClazzAssignmentWithCourseBlock().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caRequireTextSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caGroupUid = group.cgsUid
            caUid = repo.clazzAssignmentDao.insert(this)
            block = CourseBlock().apply {
                cbClazzUid = caClazzUid
                cbEntityUid = caUid
                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                cbUid = repo.courseBlockDao.insert(this)
            }
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
            mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        val systemImpl: UstadMobileSystemImpl by di.instance()

        verify(mockView, timeout(1000).times(2)).unassignedError = eq(systemImpl.getString(MessageID.unassigned, context))
        verify(mockView, timeout(1000).times(2)).addTextSubmissionVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).addFileSubmissionVisible = eq(false)
        verify(mockView, timeout(5000).times(2)).submittedCourseAssignmentSubmission = any()
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(0)
        verify(mockView, timeout(1000).times(2)).showPrivateComments = eq(false)
        verify(mockView, timeout(1000).times(2)).clazzAssignmentClazzComments = any()

    }


    //@Test
    fun givenUserIsNotStudent_whenShown_dontShowPrivateCommentsSubmissionStatusScoreAndAddFileText(){
        createPerson(ClazzEnrolment.ROLE_TEACHER)

        val testEntity = ClazzAssignmentWithCourseBlock().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caUid = repo.clazzAssignmentDao.insert(this)
            block = CourseBlock().apply {
                this.cbClazzUid = testClazz.clazzUid
                this.cbEntityUid = caUid
                this.cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                this.cbUid = repo.courseBlockDao.insert(this)
            }
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        verify(mockView, timeout(1000)).showSubmission = eq(false)
        verify(mockView, timeout(1000).times(0)).addFileSubmissionVisible
        verify(mockView, timeout(1000).times(0)).addTextSubmissionVisible
        verify(mockView, timeout(1000).times(0)).submittedCourseAssignmentSubmission
        verify(mockView, timeout(1000).times(0)).submissionMark
        verify(mockView, timeout(1000).times(0)).submissionStatus
        verify(mockView, timeout(1000).times(0)).clazzAssignmentClazzComments

    }

    //@Test
    fun givenUserClicksAddFile_whenClicked_thenGoToSelectFileView(){

        createPerson(ClazzEnrolment.ROLE_STUDENT)

        val testEntity = ClazzAssignmentWithCourseBlock().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caFileType = ClazzAssignment.FILE_TYPE_VIDEO
            caUid = repo.clazzAssignmentDao.insert(this)
            block = CourseBlock().apply {
                this.cbClazzUid = testClazz.clazzUid
                this.cbEntityUid = caUid
                this.cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                this.cbUid = repo.courseBlockDao.insert(this)
            }
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        mockView.captureLastEntityValue()

        presenter.handleAddFileClicked()

        val testNavController: UstadNavController by di.instance()
        verify(testNavController).navigate(SelectFileView.VIEW_NAME,
                mapOf(ARG_MIMETYPE_SELECTED to SelectFileView.SELECTION_MODE_VIDEO))

    }

    //@Test
    fun givenUserClicksOpenFileSubmission_whenClicked_thenShouldOpen(){

        createPerson(ClazzEnrolment.ROLE_STUDENT)
        val testEntity = ClazzAssignmentWithCourseBlock().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caFileType = ClazzAssignment.FILE_TYPE_VIDEO
            caUid = repo.clazzAssignmentDao.insert(this)
            block = CourseBlock().apply {
                this.cbClazzUid = testClazz.clazzUid
                this.cbEntityUid = caUid
                this.cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                this.cbUid = repo.courseBlockDao.insert(this)
            }
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())

        val presenter = ClazzAssignmentDetailOverviewPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        mockView.stub {
            on { entity }.thenReturn(testEntity)
        }

        mockView.captureLastEntityValue()

        val fileSubmission = CourseAssignmentSubmissionWithAttachment().apply {
            casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
            attachment = CourseAssignmentSubmissionAttachment().apply {
                casaUri = "dummy"
                casaMimeType = "video/*"
            }
        }

        presenter.handleOpenSubmission(fileSubmission)
        val systemImpl: UstadMobileSystemImpl by di.instance()
        verify(systemImpl, timeout(5000)).openFileInDefaultViewer(
            any(), any(), eq(fileSubmission.attachment!!.casaMimeType), anyOrNull())

    }


}
