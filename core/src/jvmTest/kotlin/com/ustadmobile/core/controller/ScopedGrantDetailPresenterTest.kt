
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ScopedGrantDetailView
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScopedGrantDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.util.*
import com.ustadmobile.door.lifecycle.LifecycleObserver

import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ScopedGrantDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ScopedGrantDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoScopedGrantDaoSpy: ScopedGrantDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()


        repoScopedGrantDaoSpy = spy(repo.scopedGrantDao)
        whenever(repo.scopedGrantDao).thenReturn(repoScopedGrantDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenScopedGrantExists_whenOnCreateCalled_thenScopedGrantIsSetOnView() {
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = ScopedGrant().apply {
            //set variables here
            sgUid = runBlocking { repo.scopedGrantDao.insertAsync(this@apply) }
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.sgUid.toString())

        val presenter = ScopedGrantDetailPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.sgUid, entityValSet.sgUid)
    }

    @Test
    fun givenScopedGrantExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = ScopedGrant().apply {
            //set variables here
            sgUid = runBlocking { repo.scopedGrantDao.insertAsync(this@apply) }
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.sgUid.toString())

        val presenter = ScopedGrantDetailPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        verify(mockView, timeout(5000)).entity = any()

        presenter.handleClickEdit()

        val testNavController: UstadNavController = di.direct.instance()

        verify(testNavController, timeout(5000)).navigate(eq(ScopedGrantEditView.VIEW_NAME),
            argWhere {
                it[ARG_ENTITY_UID] == testEntity.sgUid.toString()
            }, any())
    }

}