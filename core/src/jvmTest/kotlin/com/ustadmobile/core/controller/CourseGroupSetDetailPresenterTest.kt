
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.CourseGroupSetDetailView
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CourseGroupSetDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.util.*
import com.ustadmobile.door.lifecycle.LifecycleObserver

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class CourseGroupSetDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: CourseGroupSetDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoCourseGroupSetDaoSpy: CourseGroupSetDao

    private lateinit var di: DI

    private lateinit var clazz: Clazz

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()


        repoCourseGroupSetDaoSpy = spy(repo.courseGroupSetDao)
        whenever(repo.courseGroupSetDao).thenReturn(repoCourseGroupSetDaoSpy)

        //TODO: insert any entities required for all tests
        clazz = Clazz().apply{
            clazzUid = repo.clazzDao.insert(this)
        }
    }

    @Test
    fun givenCourseGroupSetExists_whenOnCreateCalled_thenCourseGroupSetIsSetOnView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = CourseGroupSet().apply {
            //set variables here
            cgsClazzUid = clazz.clazzUid
            cgsUid = repo.courseGroupSetDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.cgsUid.toString())

        val presenter = CourseGroupSetDetailPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!

    }

    @Test
    fun givenCourseGroupSetExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = CourseGroupSet().apply {
            //set variables here
            cgsClazzUid = clazz.clazzUid
            cgsUid = repo.courseGroupSetDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.cgsUid.toString())

        val presenter = CourseGroupSetDetailPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        presenter.handleClickEdit()

        val testNavController: UstadNavController = di.direct.instance()

        argumentCaptor<Map<String, String>>().apply {
            verify(testNavController, times(1)).navigate(any(), capture(), any())

            Assert.assertTrue("Same arguments were passed during navigation",
                lastValue[ARG_ENTITY_UID].toString() == testEntity.cgsUid.toString())
        }
    }

}
