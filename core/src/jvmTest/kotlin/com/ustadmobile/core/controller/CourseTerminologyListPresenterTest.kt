
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.CourseTerminologyListView
import org.mockito.kotlin.*
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.db.dao.CourseTerminologyDao
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.UstadTestRule
import org.kodein.di.DI
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.door.lifecycle.DoorState
import org.kodein.di.instance


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class CourseTerminologyListPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: CourseTerminologyListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoCourseTerminologyDaoSpy: CourseTerminologyDao

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

        repoCourseTerminologyDaoSpy = spy(repo.courseTerminologyDao)
        whenever(repo.courseTerminologyDao).thenReturn(repoCourseTerminologyDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = CourseTerminology().apply {
            //set variables here
            ctUid = repo.courseTerminologyDao.insert(this)
        }

        //TODO: add any arguments required for the presenter here e.g.
        // CourseTerminologyListView.ARG_SOME_FILTER to "filterValue"
        val presenterArgs = mapOf<String,String>()
        val presenter = CourseTerminologyListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoCourseTerminologyDaoSpy, timeout(5000)).findAllCourseTerminology()
        verify(mockView, timeout(5000)).list = any()

        //TODO: verify any other properties that the presenter should set on the view
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val presenterArgs = mapOf<String,String>()
        val testEntity = CourseTerminology().apply {
            //set variables here
            ctUid = repo.courseTerminologyDao.insert(this)
        }
        val presenter = CourseTerminologyListPresenter(context,
            presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()


        presenter.onClickCourseTerminology(testEntity)

    }

    //TODO: Add tests for other scenarios the presenter is expected to handle - e.g. different filters, etc.

}