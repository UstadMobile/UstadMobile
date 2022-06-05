
package com.ustadmobile.core.controller
import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonOnlyAndGroup
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.HtmlTextViewDetailView
import com.ustadmobile.core.view.HtmlTextViewDetailView.Companion.DISPLAY_TEXT
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SUBMITER_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:*/


class ClazzAssignmentDetailStudentProgressPresenterTest {

    private lateinit var testEntity: ClazzAssignmentWithCourseBlock
    private lateinit var mockNavController: UstadNavController
    private lateinit var repo: UmAppDatabase
    private lateinit var testClazz: Clazz

    private val loggedInPersonUid:Long = 234568

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ClazzAssignmentDetailStudentProgressView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

    private lateinit var accountManager: UstadAccountManager

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        val serverUrl = "https://dummysite.ustadmobile.app/"

        accountManager = mock{
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
            on{activeAccount}.thenReturn(UmAccount(loggedInPersonUid,"","",serverUrl))
        }

        context = Any()
        di = DI {
            import(ustadTestRule.diModule)
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

        val xObjectEntity = XObjectEntity().apply {
            objectId = "Test Assignment"
            xObjectUid = repo.xObjectDao.insert(this)
        }


        testEntity = ClazzAssignmentWithCourseBlock().apply {
            //set variables here
            caClazzUid = testClazz.clazzUid
            caRequireFileSubmission = true
            caRequireTextSubmission = true
            caPrivateCommentsEnabled = true
            caNumberOfFiles = 3
            caXObjectUid = xObjectEntity.xObjectUid
            caUid = repo.clazzAssignmentDao.insert(this)
            block = CourseBlock().apply {
                cbClazzUid = caClazzUid
                cbEntityUid = caUid
                cbLateSubmissionPenalty = 20
                cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                cbUid = repo.courseBlockDao.insert(this)
            }
        }

        createPersonAndSubmitStatement(true, loggedInPersonUid)

    }

    fun createPersonAndSubmitStatement(
        isAdmin: Boolean,
        studentId: Long,
        submitSubmission: Boolean = !isAdmin
    ): Person {
        val student = Person().apply {
            admin = false
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = studentId
            repo.insertPersonOnlyAndGroup(this)
        }


        val clazzEnrolment = ClazzEnrolment().apply {
            clazzEnrolmentPersonUid = student.personUid
            clazzEnrolmentClazzUid = testClazz.clazzUid
            clazzEnrolmentRole = if(isAdmin) ClazzEnrolment.ROLE_TEACHER else ClazzEnrolment.ROLE_STUDENT
            clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_IN_PROGRESS
            clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
        }

        val agent = AgentEntity().apply {
            agentPersonUid = studentId
            agentHomePage = accountManager.activeAccount.endpointUrl
            agentAccountName = "testuser"
            agentUid = repo.agentDao.insert(this)
        }

        if(submitSubmission){
            CourseAssignmentSubmission().apply {
                casSubmitterUid = studentId
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT
                casText = "Text"
                casAssignmentUid = testEntity.caUid
                casUid = repo.courseAssignmentSubmissionDao.insert(this)
            }

            StatementEntity().apply {
                statementVerbUid = VerbEntity.VERB_SUBMITTED_UID
                statementPersonUid = studentId
                statementClazzUid = testEntity.caUid
                xObjectUid = testEntity.caXObjectUid
                agentUid = agent.agentUid
                contextRegistration = randomUuid().toString()
                timestamp = systemTimeInMillis()
                stored = systemTimeInMillis()
                statementUid = repo.statementDao.insert(this)
            }
        }

        return student

    }


    @Test
    fun givenNoSubmissionFromStudent_whenShown_DontShowSubmitGradeAndPoints() {
        createPersonAndSubmitStatement(false , 1, false)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
            ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

        mockView.captureLastEntityValue()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(0)
    }

    @Test
    fun givenSubmissionFromStudentNotMarkedAndNoOtherStudentsToMark_whenShown_thenShowSubmitGradeAndDontShowSubmitNextGrade() {
        createPersonAndSubmitStatement(false , 1)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
                    ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

        mockView.captureLastEntityValue()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(CourseAssignmentSubmission.SUBMITTED)

    }

    @Test
    fun givenSubmissionFromStudentNotMarkedAndNoOtherStudentsToMark_whenPointsSubmittedWithinRange_thenShouldCreateStatementAndSubmitGradeTextChangedToUpdateGrade() {
        val student = createPersonAndSubmitStatement(false , 1)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
            ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

        val entity = mockView.captureLastEntityValue()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(CourseAssignmentSubmission.SUBMITTED)

        whenever(mockView.entity).thenReturn(entity)

        presenter.onClickSubmitGrade(5f)

        val systemImpl: UstadMobileSystemImpl by di.instance()

        verify(mockView, timeout(1000).atLeastOnce()).submissionStatus = eq(CourseAssignmentSubmission.MARKED)
        verify(mockView, timeout(1000).atLeastOnce()).submissionScore = argThat {
            this.camMark == 5f
        }

        val mark = repo.courseAssignmentMarkDao.getMarkOfAssignmentForStudent(testEntity.caUid, 1)
        assertNotNull(mark)
        assertEquals(5f, mark.camMark, "Mark matches")

       /* val scoreStatement = repo.statementDao.findScoreStatementForStudent(1)
        assertNotNull(scoreStatement)
        assertEquals(5L, scoreStatement.resultScoreRaw, "score statement matches mark")
*/
        verify(mockView, timeout(1000)).showSnackBar(
            eq(systemImpl.getString(MessageID.saved, context)), any(), any())


    }

    @Test
    fun givenSubmissionFromStudentAfterDeadlineAndNoOtherStudentsToMark_whenPointsSubmitted_thenShouldCreateStatementWithPenalty() {
        val student = createPersonAndSubmitStatement(false , 1)
        testEntity.block?.cbDeadlineDate = DateTime(2021, 5, 1).unixMillisLong
        repo.courseBlockDao.update(testEntity.block!!)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
            ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

        val entity = mockView.captureLastEntityValue()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(CourseAssignmentSubmission.SUBMITTED)


        whenever(mockView.entity).thenReturn(entity)

        presenter.onClickSubmitGrade(5f)

        val systemImpl: UstadMobileSystemImpl by di.instance()

        verify(mockView, timeout(1000).atLeastOnce()).submissionStatus = eq(CourseAssignmentSubmission.MARKED)
        verify(mockView, timeout(1000).atLeastOnce()).submissionScore = argThat {
            this.camMark == 4f
        }

        val mark = repo.courseAssignmentMarkDao.getMarkOfAssignmentForStudent(testEntity.caUid, 1)
        assertNotNull(mark)
        assertEquals(4f, mark.camMark, "Mark matches")

        /*val scoreStatement = repo.statementDao.findScoreStatementForStudent(1)
        assertNotNull(scoreStatement)
        assertEquals(4L, scoreStatement.resultScoreRaw, "score statement matches mark")
*/

        verify(mockView, timeout(1000)).showSnackBar(
            eq(systemImpl.getString(MessageID.saved, context)), any(), any())

    }


    @Test
    fun givenSubmissionFromStudentNotMarkedAndNoOtherStudentsToMark_whenPointsSubmittedOutOfRange_thenShowError() {
        createPersonAndSubmitStatement(false , 1)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
            ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

        mockView.captureLastEntityValue()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(CourseAssignmentSubmission.SUBMITTED)

        presenter.onClickSubmitGrade(-1f)

        val systemImpl: UstadMobileSystemImpl by di.instance()
        verify(mockView, timeout(1000)).submitMarkError = eq(
            systemImpl.getString(MessageID.grade_out_of_range, context)
                .replace("%1\$s", "10")
        )
    }

    @Test
    fun givenSubmissionFromStudentWithOtherStudentsToMark_whenPointsSubmittedWithinRange_thenCreateStatementAndMoveToNextStudent(){
        val student = createPersonAndSubmitStatement(false , 1)
        createPersonAndSubmitStatement(false, 2)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
            ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

       val entity = mockView.captureLastEntityValue()

        val systemImpl: UstadMobileSystemImpl by di.instance()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(CourseAssignmentSubmission.SUBMITTED)

        whenever(mockView.entity).thenReturn(entity)


        val noMark = repo.courseAssignmentMarkDao.getMarkOfAssignmentForStudent(testEntity.caUid, 1)
        assertNull(noMark)

        presenter.onClickSubmitGradeAndMarkNext(5f)

        verify(mockView, timeout(1000).atLeastOnce()).submissionStatus = eq(CourseAssignmentSubmission.MARKED)
        verify(mockView, timeout(1000).atLeastOnce()).submissionScore = argThat {
            this.camMark == 5f
        }

        val mark = repo.courseAssignmentMarkDao.getMarkOfAssignmentForStudent(testEntity.caUid, 1)
        assertNotNull(mark)
        assertEquals(5f, mark.camMark, "Mark matches")

        /*val scoreStatement = repo.statementDao.findScoreStatementForStudent(1)
        assertNotNull(scoreStatement)
        assertEquals(5L, scoreStatement.resultScoreRaw, "score statement matches mark")
*/

        verify(mockView, timeout(1000)).showSnackBar(eq(systemImpl.getString(MessageID.saved, context)), any(), any())
        verify(systemImpl, timeout(1000)).go(eq(ClazzAssignmentDetailStudentProgressView.VIEW_NAME),
            eq(mapOf(ARG_SUBMITER_UID to 2.toString(),
                ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
                UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString())), eq(context),
            any()
        )

    }

    @Test
    fun givenSubmissionFromStudent_whenTextSubmissionClicked_thenNavigateToTextDetail() {
        val student = createPersonAndSubmitStatement(false , 1)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
            ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

        val entity = mockView.captureLastEntityValue()

        val systemImpl: UstadMobileSystemImpl by di.instance()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(CourseAssignmentSubmission.SUBMITTED)

        whenever(mockView.entity).thenReturn(entity)

        presenter.onClickOpenSubmission(CourseAssignmentSubmissionWithAttachment().apply {
            casType = CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT
        })

        val nav: UstadNavController by di.instance()

        verify(nav, timeout(1000)).navigate(
            eq(HtmlTextViewDetailView.VIEW_NAME),
            eq(mapOf(DISPLAY_TEXT to "")),
            any()
        )


    }

    @Test
    fun givenSubmissionFromStudent_whenFileSubmissionClicked_thenNavigateToFileOpener() {
        val student = createPersonAndSubmitStatement(false , 1)

        val presenterArgs = mapOf(
            UstadView.ARG_SUBMITER_UID to 1.toString(),
            ARG_CLAZZ_ASSIGNMENT_UID to testEntity.caUid.toString(),
            UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()
        )

        val presenter = ClazzAssignmentDetailStudentProgressPresenter(context, presenterArgs, mockView,
            di, mockLifecycleOwner)

        presenter.onCreate(null)

        val entity = mockView.captureLastEntityValue()

        val systemImpl: UstadMobileSystemImpl by di.instance()

        verify(mockView, timeout(1000).times(2)).markNextStudentVisible = eq(false)
        verify(mockView, timeout(1000).times(2)).submitButtonVisible = eq(true)
        verify(mockView, timeout(1000).times(2)).submissionScore = eq(null)
        verify(mockView, timeout(1000).times(2)).submissionStatus = eq(CourseAssignmentSubmission.SUBMITTED)

        whenever(mockView.entity).thenReturn(entity)


        presenter.onClickOpenSubmission(CourseAssignmentSubmissionWithAttachment().apply {
            casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
            attachment = CourseAssignmentSubmissionAttachment().apply {
                casaUri = "door-attachment://CourseAssignmentSubmissionAttachment/dummy.mp4"
                casaMimeType = "video/mp4"
            }
        })

        verify(systemImpl, timeout(1000)).openFileInDefaultViewer(
            eq(context),
           any(),
            eq("video/mp4"),
            anyOrNull(),
        )

    }




}
