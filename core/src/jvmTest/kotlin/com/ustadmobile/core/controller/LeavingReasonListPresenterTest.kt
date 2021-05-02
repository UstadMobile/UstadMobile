
package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LeavingReasonDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.LeavingReason
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */

class LeavingReasonListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: LeavingReasonListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoLeavingReasonDaoSpy: LeavingReasonDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        di = DI {
            import(ustadTestRule.diModule)
        }
        val repo: UmAppDatabase by di.activeRepoInstance()
        context = Any()
        repoLeavingReasonDaoSpy = spy(repo.leavingReasonDao)
        whenever(repo.leavingReasonDao).thenReturn(repoLeavingReasonDaoSpy)
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = LeavingReason().apply {
            //set variables here
            leavingReasonTitle = "Moved"
            leavingReasonUid = repo.leavingReasonDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>()
        val presenter = LeavingReasonListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoLeavingReasonDaoSpy, timeout(5000)).findAllReasons()
        verify(mockView, timeout(5000)).list = any()

    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {

        val repo: UmAppDatabase by di.activeRepoInstance()
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val testEntity = LeavingReason().apply {
            //set variables here
            leavingReasonTitle = "Moved"
            leavingReasonUid = repo.leavingReasonDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>()
        val presenter = LeavingReasonListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        presenter.onClickLeavingReason(testEntity)

        verify(systemImpl, timeout(5000)).go(eq(LeavingReasonEditView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.leavingReasonUid.toString())), any())

    }

}
