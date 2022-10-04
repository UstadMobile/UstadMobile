
package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CourseGroupSetDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.CourseGroupSetDetailView
import com.ustadmobile.core.view.CourseGroupSetListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseGroupSet
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance
import org.mockito.kotlin.*


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class CourseGroupSetListPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: CourseGroupSetListView

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

        clazz = Clazz().apply{
            clazzUid = repo.clazzDao.insert(this)
        }

    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = CourseGroupSet().apply {
            //set variables here
            cgsClazzUid = clazz.clazzUid
            cgsUid = repo.courseGroupSetDao.insert(this)
        }

        //TODO: add any arguments required for the presenter here e.g.
        // CourseGroupSetListView.ARG_SOME_FILTER to "filterValue"
        val presenterArgs = mutableMapOf<String,String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = clazz.clazzUid.toString()
        val presenter = CourseGroupSetListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoCourseGroupSetDaoSpy, timeout(5000)).findAllCourseGroupSetForClazz(clazz.clazzUid)
        verify(mockView, timeout(5000)).list = any()
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val presenterArgs = mutableMapOf<String,String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = clazz.clazzUid.toString()
        val testEntity = CourseGroupSet().apply {
            //set variables here
            cgsClazzUid = clazz.clazzUid
            cgsUid = repo.courseGroupSetDao.insert(this)
        }

        val presenter = CourseGroupSetListPresenter(context,
            presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.handleClickEntry(testEntity)


        val systemImpl: UstadMobileSystemImpl by di.instance()


        verify(systemImpl, timeout(5000)).go(
            eq(CourseGroupSetDetailView.VIEW_NAME),
           eq(mapOf(ARG_ENTITY_UID to testEntity.cgsUid.toString(),
               ARG_CLAZZUID to clazz.clazzUid.toString())),
            any())
    }

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}