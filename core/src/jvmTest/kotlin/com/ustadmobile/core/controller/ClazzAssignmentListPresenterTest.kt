
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import org.kodein.di.DI
import org.kodein.di.instance
import org.mockito.kotlin.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:*/


class ClazzAssignmentListPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockViewClazz: ClazzAssignmentListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockViewClazz = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        context = Any()
        repoClazzAssignmentDaoSpy = spy(repo.clazzAssignmentDao)
        whenever(repo.clazzAssignmentDao).thenReturn(repoClazzAssignmentDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = ClazzAssignment().apply {
            //set variables here
            caUid = repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>()
        val presenter = ClazzAssignmentListPresenter(context,
                presenterArgs, mockViewClazz, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoClazzAssignmentDaoSpy, timeout(5000)).getAllAssignments(any(), any(),
                any(), any(), any(), any())
        verify(mockViewClazz, timeout(5000)).list = any()

        //TODO: verify any other properties that the presenter should set on the view
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val presenterArgs = mapOf<String,String>()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val testEntity = ClazzAssignmentWithMetrics().apply {
            //set variables here
            caUid = repo.clazzAssignmentDao.insert(this)
        }
        val presenter = ClazzAssignmentListPresenter(context,
                presenterArgs, mockViewClazz, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockViewClazz.waitForListToBeSet()


        presenter.onClickAssignment(testEntity)


        verify(systemImpl, timeout(5000)).go(eq(ClazzAssignmentDetailView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())), any())
    }

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}
