
package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PeerReviewerAllocationDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockEditView
import com.ustadmobile.core.view.PeerReviewerAllocationEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.mockito.kotlin.*


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class PeerReviewerAllocationEditPresenterTest {

    private lateinit var clazzAssignment: ClazzAssignment
    private lateinit var clazz: Clazz

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: PeerReviewerAllocationEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoPeerReviewerAllocationDaoSpy: PeerReviewerAllocationDao

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

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoPeerReviewerAllocationDaoSpy = spy(repo.peerReviewerAllocationDao)
        whenever(repo.peerReviewerAllocationDao).thenReturn(repoPeerReviewerAllocationDaoSpy)

        clazz = Clazz().apply {
            clazzUid = repo.clazzDao.insert(this)
        }

        val group = CourseGroupSet().apply {
            cgsName = "Set 1"
            cgsTotalGroups = 3
            cgsUid = repo.courseGroupSetDao.insert(this)
        }

        repeat(6){
            val person = Person().apply{
                firstNames = "Hello $it"
                lastName = "end"
                personUid = (1000 + it).toLong()
            }
            CourseGroupMember().apply {
                cgmSetUid = group.cgsUid
                cgmPersonUid = person.personUid
                cgmGroupNumber = (it % group.cgsTotalGroups) + 1
                cgmUid = repo.courseGroupMemberDao.insert(this)
            }
        }

        clazzAssignment = ClazzAssignment().apply {
            caClazzUid = clazz.clazzUid
            caGroupUid = group.cgsUid
            caPeerReviewerCount = 2
            caMarkingType = ClazzAssignment.MARKED_BY_PEERS
            caUid = repo.clazzAssignmentDao.insert(this)
        }

    }

    @Test
    fun givenGroups_whenRandomAssignToPeers_thenShowDistributed(){

        val presenterArgs = mapOf(
            UstadView.ARG_CLAZZ_ASSIGNMENT_UID to clazzAssignment.caUid.toString(),
            PeerReviewerAllocationEditView.ARG_ASSIGNMENT_GROUP to clazzAssignment.caGroupUid.toString(),
            ARG_CLAZZUID to clazz.clazzUid.toString(),
            PeerReviewerAllocationEditView.ARG_REVIEWERS_COUNT to clazzAssignment.caPeerReviewerCount.toString()
        )

        val presenter = PeerReviewerAllocationEditPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        nullableArgumentCaptor<List<AssignmentSubmitterWithAllocations>>().apply {
            verify(mockView, timeout(1000).atLeastOnce()).submitterListWithAllocations = capture()
            val value = lastValue!!
            whenever(mockView.submitterListWithAllocations).thenReturn(value)
        }

        presenter.handleRandomAssign()

        nullableArgumentCaptor<List<AssignmentSubmitterWithAllocations>>().apply {
            verify(mockView, timeout(1000).atLeastOnce()).submitterListWithAllocations = capture()

            val result = lastValue!!

            val toBucket = mutableMapOf<Long, List<Long>>()
            val size = 2
            var equallyAssigned = true
            var allAssigned = true
            result.forEach {
                val toList = mutableListOf<Long>()
                it.allocations?.forEach { peer ->
                    toList.add(peer.praMarkerSubmitterUid)
                    if(peer.praMarkerSubmitterUid == 0L){
                        allAssigned = false
                    }
                }
                if(toList.size != size){
                    equallyAssigned = true
                }
                toBucket[it.submitterUid] = toList
            }

            Assert.assertEquals("all equally assigned", true, equallyAssigned)
            Assert.assertEquals("all assigned", true, allAssigned)
        }

    }




}