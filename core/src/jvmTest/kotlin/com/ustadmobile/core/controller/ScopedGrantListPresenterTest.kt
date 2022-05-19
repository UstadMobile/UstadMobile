
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ScopedGrantListView
import org.mockito.kotlin.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ScopedGrantDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.core.util.UstadTestRule
import org.kodein.di.DI
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.lib.db.entities.Clazz


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ScopedGrantListPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ScopedGrantListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoScopedGrantDaoSpy: ScopedGrantDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoScopedGrantDaoSpy = spy(repo.scopedGrantDao)
        whenever(repo.scopedGrantDao).thenReturn(repoScopedGrantDaoSpy)
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val presenterArgs = mapOf(
            ScopedGrantListView.ARG_FILTER_TABLE_ID to Clazz.TABLE_ID.toString(),
            ScopedGrantListView.ARG_FILTER_ENTITY_UID to "42")
        val presenter = ScopedGrantListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoScopedGrantDaoSpy, timeout(5000)).findByTableIdAndEntityUidWithNameAsDataSource(
            Clazz.TABLE_ID, 42L)
        verify(mockView, timeout(5000)).list = any()
    }

    @Test
    fun givenUserHasDelegatePermission_whenOnCheckNewPermissionCalled_thenShouldReturnTrue() {


    }


//    @Test
//    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
//        val repo: UmAppDatabase by di.activeRepoInstance()
//        val presenterArgs = mapOf<String,String>()
//        val testEntity = ScopedGrant().apply {
//            //set variables here
//            scopedGrantUid = repo.scopedGrantDao.insert(this)
//        }
//        val presenter = ScopedGrantListPresenter(context,
//            presenterArgs, mockView, di, mockLifecycleOwner)
//        presenter.onCreate(null)
//        mockView.waitForListToBeSet()
//
//
//        presenter.handleClickEntry(testEntity)
//
//
//        val systemImpl: UstadMobileSystemImpl by di.instance()
//
//
//        verify(systemImpl, timeout(5000)).go(eq(ScopedGrantDetailView.VIEW_NAME),
//            argWhere {
//                it.get(ARG_ENTITY_UID) == testEntity.scopedGrantUid.toString()
//            }, any())
//    }

}