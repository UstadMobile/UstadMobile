
package com.ustadmobile.core.controller


import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.*


class ClazzAssignmentEditPresenterTest {


    private lateinit var repo: UmAppDatabase

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ClazzAssignmentEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var testClazz: Clazz


    private lateinit var testNavController: UstadNavController

    @Before
    fun setup() {
        mockView = mockEditView { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        repo = di.directActiveRepoInstance()
        testNavController = di.direct.instance()

        val school = School().apply{
            schoolTimeZone = "UTC"
            schoolUid = repo.schoolDao.insert(this)
        }

        testClazz = Clazz().apply {
            clazzName = "Spelling Clazz"
            clazzSchoolUid = school.schoolUid
            clazzStartTime = DateTime(2020, 8, 10).unixMillisLong
            clazzUid = repo.clazzDao.insert(this)
        }

    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_RESULT_DEST_VIEWNAME] = ClazzEdit2View.VIEW_NAME
        presenterArgs[UstadView.ARG_RESULT_DEST_KEY] = "CourseBlockWithEntity"
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()

        testNavController.navigate(ClazzEdit2View.VIEW_NAME, mapOf())
        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        initialEntity.assignment!!.caTitle = "Test Clazz Assignment"
        initialEntity.cbMaxPoints = 2
        initialEntity.assignment!!.caRequireFileSubmission = false
        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(Long.MAX_VALUE)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        verify(testNavController, timeout(1000)).popBackStack(ClazzEdit2View.VIEW_NAME, false)

        val resultSavedJson : String? = testNavController.currentBackStackEntry?.savedStateHandle
            ?.get("CourseBlockWithEntity")
        val resultSaved: CourseBlockWithEntity = Json.decodeFromString(
            ListSerializer(CourseBlockWithEntity.serializer()), resultSavedJson!!).first()

        Assert.assertEquals("title match for CourseBlock saved to JSON is equal to valid courseBlock set on view",
            initialEntity.assignment!!.caTitle, resultSaved.assignment?.caTitle)

        Assert.assertEquals("maxPoints match for CourseBlock saved to JSON is equal to valid courseBlock set on view",
            initialEntity.cbMaxPoints, resultSaved.cbMaxPoints)

        Assert.assertEquals("type match for CourseBlock saved to JSON is equal to valid courseBlock set on view",
            initialEntity.cbType, resultSaved.cbType)

        Assert.assertEquals("file disabled match for CourseBlock saved to JSON is equal to valid courseBlock set on view",
            initialEntity.assignment!!.caRequireFileSubmission, resultSaved.assignment?.caRequireFileSubmission)

        Assert.assertEquals("clazzUid match for CourseBlock saved to JSON is equal to valid courseBlock set on view",
            testClazz.clazzUid, resultSaved.assignment?.caClazzUid)

    }

    @Test
    fun givenAssignmentEditedWithTextAndFileDisabled_whenClickedSave_showErrorMessage(){

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()

        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment!!.caRequireFileSubmission = false
        initialEntity.assignment!!.caRequireTextSubmission = false

        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(Long.MAX_VALUE)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(1000)).showSnackBar(eq(systemImpl.getString(MessageID.text_file_submission_error, context)), any(), any())
    }

    @Test
    fun givenAssignmentEditedWithPointsSetToZero_whenClickedSave_showErrorMessage(){

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()

        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment!!.caRequireFileSubmission = false
        initialEntity.cbMaxPoints = 0

        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(Long.MAX_VALUE)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(1000)).caMaxPointsError = eq(systemImpl.getString(MessageID.field_required_prompt, context))


    }

    @Test
    fun givenAssignmentEditedWithDeadlineBeforeStartDate_whenClickedSave_showErrorMessage(){

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()

        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment!!.caRequireFileSubmission = false
        initialEntity.cbMaxPoints = 0

        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(DateTime(2021, 5, 1).unixMillisLong)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(1000)).caDeadlineError = eq(systemImpl.getString(MessageID.end_is_before_start_error, context))


    }

    @Test
    fun givenAssignmentEditedWithGracePeriodBeforeDeadlineDate_whenClickedSave_showErrorMessage(){

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()

        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment!!.caRequireFileSubmission = false
        initialEntity.cbMaxPoints = 0

        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(DateTime(2021, 5, 1).unixMillisLong)
        whenever(mockView.gracePeriodDate).thenReturn(DateTime(2021, 4, 1).unixMillisLong)

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(1000)).caGracePeriodError = eq(systemImpl.getString(MessageID.after_deadline_date_error, context))


    }

    @Test
    fun givenExistingAssignmentGroupUidWasChanged_whenSubmissionIsMadeBeforeSave_thenShowError(){

        val assignment = CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caGroupUid = 1
                caTitle = "AssignmentA"
                caClazzUid = testClazz.clazzUid
                caUid = repo.clazzAssignmentDao.insert(this)
            }
            cbClazzUid = testClazz.clazzUid
            cbEntityUid = assignment!!.caUid
            cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
            cbTitle = "AssignmentA"
            cbUid = repo.courseBlockDao.insert(this)
        }

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()
        presenterArgs[UstadEditView.ARG_ENTITY_JSON] = safeStringify(di, CourseBlockWithEntity.serializer(), assignment)

        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment?.caGroupUid = 2

        verify(mockView, timeout(1000)).groupSetEnabled = eq(true)

        val submission = CourseAssignmentSubmission().apply {
            casAssignmentUid = assignment.assignment!!.caUid
            casSubmitterUid = 1
            casUid = repo.courseAssignmentSubmissionDao.insert(this)
        }

        verify(mockView, timeout(5000)).groupSetEnabled = eq(false)

        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(1000)).showSnackBar(eq(systemImpl.getString(MessageID.error, context)), any(), any())

    }







}
