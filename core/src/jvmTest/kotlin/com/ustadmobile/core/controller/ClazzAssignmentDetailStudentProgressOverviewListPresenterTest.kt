
package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.*
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzAssignmentRollUp
import com.ustadmobile.util.test.ext.insertTestClazzAssignment
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.kodein.di.DI
import org.kodein.di.instance
import org.mockito.kotlin.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */

class ClazzAssignmentDetailStudentProgressOverviewListPresenterTest {

    private var assignmentRollUp: ClazzAssignmentRollUp? = null

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzAssignmentDetailStudentProgressOverviewListView

    private lateinit var context: Any

    private lateinit var di: DI

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

    //@Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()
        di = DI {
            import(ustadTestRule.diModule)
        }
        val repo: UmAppDatabase by di.activeRepoInstance()
        assignmentRollUp = repo.insertTestClazzAssignment(endpoint = di.onActiveAccountDirect().instance<UstadAccountManager>().activeEndpoint.url)

        repoClazzAssignmentDaoSpy = spy(repo.clazzAssignmentDao)
        whenever(repo.clazzAssignmentDao).thenReturn(repoClazzAssignmentDaoSpy)

    }

    //@Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnViewAndClickGoesToDetail() {

        val db: UmAppDatabase by di.activeDbInstance()
        val clazzAssignment = runBlocking {
            db.clazzAssignmentDao.findByUidAsync(assignmentRollUp!!.cacheClazzAssignmentUid)!!
        }

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[ARG_ENTITY_UID] = assignmentRollUp?.cacheClazzAssignmentUid.toString()
        presenterArgs[ARG_CLAZZUID] = 100.toString()
        val presenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
       /* runBlocking {
            verify(repoClazzAssignmentDaoSpy, timeout(5000))
                    .getProgressSummaryForAssignment()t(eq(clazzAssignment.caClazzUid), any(),
                            eq(assignmentRollUp!!.cacheClazzAssignmentUid))
        }*/

       /* verify(repoClazzAssignmentDaoSpy, timeout(5000))
                .getSubmitterListForAssignment(eq(assignmentRollUp!!.cacheClazzAssignmentUid),
                        eq(clazzAssignment.caClazzUid), any(), any(), any())
        verify(mockView, timeout(5000)).progressSummary = any()
        verify(mockView, timeout(5000)).list = any()

        presenter.onClickPerson(PersonWithAttemptsSummary().apply {
            this.personUid = assignmentRollUp!!.cachePersonUid
        })
*/
        val systemImpl: UstadMobileSystemImpl by di.instance()
        verify(systemImpl, timeout(5000)).go(eq(ClazzAssignmentDetailStudentProgressView.VIEW_NAME),
                eq(mapOf(ARG_PERSON_UID to assignmentRollUp!!.cachePersonUid.toString(),
                        ARG_CLAZZ_ASSIGNMENT_UID to assignmentRollUp!!.cacheClazzAssignmentUid.toString(),
                        ARG_CLAZZUID to 100.toString())),
                any())

    }


}
