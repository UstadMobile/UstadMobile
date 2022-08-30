
package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.ScheduleDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */

class ClazzDetailOverviewPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzDetailOverviewView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoClazzDaoSpy: ClazzDao

    private lateinit var repoScheduleSpy: ScheduleDao

    private lateinit var di: DI

    private lateinit var mockNavController: UstadNavController

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val db: UmAppDatabase by di.activeDbInstance()

        val repo: UmAppDatabase by di.activeRepoInstance()

        mockNavController = di.direct.instance()

        repoClazzDaoSpy = spy(repo.clazzDao).also {
            whenever(repo.clazzDao).thenReturn(it)
        }

        repoScheduleSpy = spy(repo.scheduleDao).also {
            whenever(repo.scheduleDao).thenReturn(it)
        }
    }

    @Test
    fun givenClazzExists_whenOnCreateCalled_thenClazzIsSetOnView() {
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = repo.clazzDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())
        val presenter = ClazzDetailOverviewPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)


        presenter.onCreate(null)


        nullableArgumentCaptor<ClazzWithDisplayDetails>().apply {
            verify(mockView, timeout(5000).atLeastOnce()).entity = capture()
            Assert.assertEquals("Expected entity was set on view",
                    testEntity.clazzUid, lastValue!!.clazzUid)
        }

        verify(mockView, timeout(5000)).scheduleList = any()
        verifyBlocking(repoScheduleSpy, timeout(5000)) {findAllSchedulesByClazzUid(testEntity.clazzUid)}
    }

    @Test
    fun givenClazzExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = repo.clazzDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())
        val presenter = ClazzDetailOverviewPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickEdit()

        verify(mockNavController, timeout(5000)).navigate(
            eq(ClazzEdit2View.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())),
            any()
        )
    }
}
