
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.db.dao.ScheduleDao
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.kodein.di.DI

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ClazzDetailOverviewPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ClazzDetailOverviewView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzDaoSpy: ClazzDao

    private lateinit var repoScheduleSpy: ScheduleDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoClazzDaoSpy = spy(clientDbRule.db.clazzDao).also {
            whenever(clientDbRule.db.clazzDao).thenReturn(it)
        }

        repoScheduleSpy = spy(clientDbRule.db.scheduleDao).also {
            whenever(clientDbRule.db.scheduleDao).thenReturn(it)
        }

        di = DI {
            import(systemImplRule.diModule)
            import(clientDbRule.diModule)
        }


        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenClazzExists_whenOnCreateCalled_thenClazzIsSetOnView() {
        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = clientDbRule.db.clazzDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())
        val presenter = ClazzDetailOverviewPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)


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
        val testEntity = Clazz().apply {
            //set variables here
            clazzUid = clientDbRule.db.clazzDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())
        val presenter = ClazzDetailOverviewPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)

        presenter.handleClickEdit()

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(ClazzEdit2View.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())), any())
    }
}