/*

package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentWithMetricsDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzAssignmentWithMetricsDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID

*/
/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 *//*

class ClazzAssignmentDetailStudentProgressOverviewListPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ClazzAssignmentDetailStudentProgressOverviewListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzAssignmentWithMetricsDaoSpy: ClazzAssignmentWithMetricsDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoClazzAssignmentWithMetricsDaoSpy = spy(clientDbRule.db.clazzAssignmentWithMetricsDao)
        whenever(clientDbRule.db.clazzAssignmentWithMetricsDao).thenReturn(repoClazzAssignmentWithMetricsDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val testEntity = ClazzAssignmentWithMetrics().apply {
            //set variables here
            clazzAssignmentWithMetricsUid = clientDbRule.db.clazzAssignmentWithMetricsDao.insert(this)
        }

        //TODO: add any arguments required for the presenter here e.g.
        // ClazzAssignmentDetailStudentProgressListView.ARG_SOME_FILTER to "filterValue"
        val presenterArgs = mapOf<String,String>()
        val presenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzAssignmentWithMetricsDaoSpy, timeout(5000)).findByClazzAssignmentWithMetricsUidAsFactory()
        verify(mockView, timeout(5000)).list = any()

        //TODO: verify any other properties that the presenter should set on the view
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenterArgs = mapOf<String,String>()
        val testEntity = ClazzAssignmentWithMetrics().apply {
            //set variables here
            clazzAssignmentWithMetricsUid = clientDbRule.db.clazzAssignmentWithMetricsDao.insert(this)
        }
        val presenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.handleClickEntry(testEntity)


        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(ClazzAssignmentWithMetricsDetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.clazzAssignmentWithMetricsUid.toString())), any())
    }

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}*/
