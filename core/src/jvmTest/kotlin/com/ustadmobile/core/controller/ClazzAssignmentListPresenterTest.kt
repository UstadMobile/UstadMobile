/*

package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.AssignmentDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID

*/
/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 *//*

class ClazzAssignmentListPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockViewClazz: ClazzAssignmentListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

    @Before
    fun setup() {
        mockViewClazz = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoClazzAssignmentDaoSpy = spy(clientDbRule.db.assignmentDao)
        whenever(clientDbRule.db.assignmentDao).thenReturn(repoClazzAssignmentDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val testEntity = ClazzAssignment().apply {
            //set variables here
            assignmentUid = clientDbRule.db.assignmentDao.insert(this)
        }

        //TODO: add any arguments required for the presenter here e.g.
        // AssignmentListView.ARG_SOME_FILTER to "filterValue"
        val presenterArgs = mapOf<String,String>()
        val presenter = ClazzAssignmentListPresenter(context,
                presenterArgs, mockViewClazz, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzAssignmentDaoSpy, timeout(5000)).findByAssignmentUidAsFactory()
        verify(mockViewClazz, timeout(5000)).list = any()

        //TODO: verify any other properties that the presenter should set on the view
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenterArgs = mapOf<String,String>()
        val testEntity = ClazzAssignment().apply {
            //set variables here
            assignmentUid = clientDbRule.db.assignmentDao.insert(this)
        }
        val presenter = ClazzAssignmentListPresenter(context,
                presenterArgs, mockViewClazz, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)
        mockViewClazz.waitForListToBeSet()


        presenter.handleClickEntry(testEntity)


        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(AssignmentDetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.assignmentUid.toString())), any())
    }

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}*/
