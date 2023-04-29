
package com.ustadmobile.core.controller


import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.assignRandomly
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.*
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

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var testClazz: Clazz

    private val loggedInPersonUid:Long = 234568

    private var testNavController: UstadNavController? = null

    @Before
    fun setup() {
        mockView = mockEditView { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
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

        repeat(6){
            val person = Person().apply{
                firstNames = "Hello $it"
                lastName = "end"
                personUid = (1000 + it).toLong()
                repo.personDao.insert(this)
            }
            val clazzEnrolment = ClazzEnrolment().apply {
                clazzEnrolmentClazzUid = testClazz.clazzUid
                clazzEnrolmentPersonUid = person.personUid
                clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
                clazzEnrolmentUid = repo.clazzEnrolmentDao.insert(this)
            }
        }

    }

    @After
    fun close(){
        testNavController = null
    }



    @Test
    fun givenExistingAssignmentMarkingTypeWasChanged_whenSubmissionMarkedBeforeSave_thenShowError(){

        val assignment = CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caMarkingType = ClazzAssignment.MARKED_BY_COURSE_LEADER
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

        testNavController!!.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment?.caMarkingType = ClazzAssignment.MARKED_BY_PEERS

        verify(mockView, timeout(1000)).markingTypeEnabled = eq(true)

        val submission = CourseAssignmentMark().apply {
            camAssignmentUid = assignment.assignment!!.caUid
            camSubmitterUid = 1
            camUid = repo.courseAssignmentMarkDao.insert(this)
        }

        verify(mockView, timeout(5000)).markingTypeEnabled = eq(false)

        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(1000)).showSnackBar(eq(systemImpl.getString(MessageID.error, context)), any(), any())

    }



    @Test
    fun givenExistingPeerAllocations_whenPeerCountIncreases_thenAddMorePeerAllocations(){

        val coursesBlockWithEntity = CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caTitle = "AssignmentA"
                caClazzUid = testClazz.clazzUid
                caMarkingType = ClazzAssignment.MARKED_BY_PEERS
                caPeerReviewerCount = 1
                caUid = repo.clazzAssignmentDao.insert(this)
            }
            cbClazzUid = testClazz.clazzUid
            cbEntityUid = assignment!!.caUid
            cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
            cbTitle = "AssignmentA"
            cbUid = repo.courseBlockDao.insert(this)
        }

        val submitters = runBlocking {
            val submitterCount = repo.clazzAssignmentDao.getSubmitterCountFromAssignment(0, testClazz.clazzUid, "")
            println("submitter count $submitterCount")
            println("clazzUid ${testClazz.clazzUid}")
            repo.clazzAssignmentDao.getSubmitterListForAssignmentList(0, testClazz.clazzUid, "")
        }

        val toBucket = submitters.assignRandomly(coursesBlockWithEntity.assignment!!.caPeerReviewerCount)

        val peerAllocationList = mutableListOf<PeerReviewerAllocation>()
        submitters.forEach { submitter ->
            val toList = toBucket[submitter.submitterUid] ?: listOf()
            toList.forEach {
                peerAllocationList.add(PeerReviewerAllocation().apply {
                    praAssignmentUid = coursesBlockWithEntity.assignment!!.caUid
                    praMarkerSubmitterUid = it
                    praToMarkerSubmitterUid = submitter.submitterUid
                    praUid = repo.peerReviewerAllocationDao.insert(this)
                })
            }
        }
        coursesBlockWithEntity.assignmentPeerAllocations = peerAllocationList

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_RESULT_DEST_VIEWNAME] = ClazzEdit2View.VIEW_NAME
        presenterArgs[UstadView.ARG_RESULT_DEST_KEY] = "CourseBlockWithEntity"
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()
        presenterArgs[UstadEditView.ARG_ENTITY_JSON] = safeStringify(di, CourseBlockWithEntity.serializer(), coursesBlockWithEntity)

        testNavController!!.navigate(ClazzEdit2View.VIEW_NAME, mapOf())
        testNavController!!.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment!!.caPeerReviewerCount = 2
        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(Long.MAX_VALUE)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        presenter.handleClickSave(initialEntity)

        verify(testNavController, timeout(1000))!!.popBackStack(ClazzEdit2View.VIEW_NAME, false)

        val resultSavedJson : String? = testNavController!!.currentBackStackEntry?.savedStateHandle
            ?.get("CourseBlockWithEntity")
        val resultSaved: CourseBlockWithEntity = Json.decodeFromString(
            ListSerializer(CourseBlockWithEntity.serializer()), resultSavedJson!!).first()

        Assert.assertEquals("peer allocations increased", 12, resultSaved.assignmentPeerAllocations!!.size)

    }

    @Test
    fun givenExistingPeerAllocations_whenPeerCountDecreases_thenAllocationsToRemoveNotEmpty(){

        val courseBlockWithEntity = CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caTitle = "AssignmentA"
                caClazzUid = testClazz.clazzUid
                caMarkingType = ClazzAssignment.MARKED_BY_PEERS
                caPeerReviewerCount = 3
                caUid = repo.clazzAssignmentDao.insert(this)
            }
            cbClazzUid = testClazz.clazzUid
            cbEntityUid = assignment!!.caUid
            cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
            cbTitle = "AssignmentA"
            cbUid = repo.courseBlockDao.insert(this)
        }

        val submitters = runBlocking {
            repo.clazzAssignmentDao.getSubmitterListForAssignmentList(0, testClazz.clazzUid, "")
        }

        val toBucket = submitters.assignRandomly(courseBlockWithEntity.assignment!!.caPeerReviewerCount)

        val peerAllocationList = mutableListOf<PeerReviewerAllocation>()
        submitters.forEach { submitter ->
            val toList = toBucket[submitter.submitterUid] ?: listOf()
            toList.forEach {
                peerAllocationList.add(PeerReviewerAllocation().apply {
                    praAssignmentUid = courseBlockWithEntity.assignment!!.caUid
                    praMarkerSubmitterUid = it
                    praToMarkerSubmitterUid = submitter.submitterUid
                    praUid = repo.peerReviewerAllocationDao.insert(this)
                })
            }
        }
        courseBlockWithEntity.assignmentPeerAllocations = peerAllocationList

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_RESULT_DEST_VIEWNAME] = ClazzEdit2View.VIEW_NAME
        presenterArgs[UstadView.ARG_RESULT_DEST_KEY] = "CourseBlockWithEntity"
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()
        presenterArgs[UstadEditView.ARG_ENTITY_JSON] = safeStringify(di, CourseBlockWithEntity.serializer(), courseBlockWithEntity)

        testNavController!!.navigate(ClazzEdit2View.VIEW_NAME, mapOf())
        testNavController!!.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)

        val presenter = ClazzAssignmentEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity: CourseBlockWithEntity = mockView.captureLastEntityValue()!!

        initialEntity.assignment!!.caPeerReviewerCount = 1
        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(Long.MAX_VALUE)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        presenter.handleClickSave(initialEntity)

        verify(testNavController, timeout(1000))!!.popBackStack(ClazzEdit2View.VIEW_NAME, false)

        val resultSavedJson : String? = testNavController!!.currentBackStackEntry?.savedStateHandle
            ?.get("CourseBlockWithEntity")
        val resultSaved: CourseBlockWithEntity = Json.decodeFromString(
            ListSerializer(CourseBlockWithEntity.serializer()), resultSavedJson!!).first()

        Assert.assertEquals("peer allocations increased", 6, resultSaved.assignmentPeerAllocations!!.size)
        Assert.assertEquals("peer allocations to remove is same", 12, resultSaved.assignmentPeerAllocationsToRemove!!.size)

    }





}
