
package com.ustadmobile.core.controller


import com.google.gson.Gson
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.util.test.waitUntilAsyncOrTimeout
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.School
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIProperty
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.*


/*
*
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
*/



class ClazzAssignmentEditPresenterTest {


    private lateinit var repo: UmAppDatabase

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ClazzAssignmentEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var testClazz: Clazz


    private lateinit var testNavController: UstadNavController

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

        repo = di.directActiveRepoInstance()
        testNavController = di.direct.instance()

        val school = School().apply{
            schoolTimeZone = "UTC"
            schoolUid = repo.schoolDao.insert(this)
        }

        testClazz = Clazz().apply {
            clazzName = "Spelling Clazz"
            clazzSchoolUid = school.schoolUid
            clazzStartTime = DateTime(2020, 8, 10).unixMillisLong
            clazzUid = repo.clazzDao.insert(this)
        }



    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[UstadView.ARG_CLAZZUID] = testClazz.clazzUid.toString()

        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)
        val presenter = ClazzAssignmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        initialEntity.caTitle = "Test Clazz Assignment"
        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(Long.MAX_VALUE)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        presenter.handleClickSave(initialEntity)

        verify(systemImpl, timeout(5000)).go(
                eq(ClazzAssignmentDetailView.VIEW_NAME),
                any(), any(), any())


        runBlocking {
            repo.waitUntil(5000, listOf("ClazzAssignment")) {
                repo.clazzAssignmentDao.findClazzAssignment()?.caTitle == "Test Clazz Assignment"
            }
            val entitySaved = repo.clazzAssignmentDao.findClazzAssignment()
            Assert.assertEquals("Entity was saved to database", "Test Clazz Assignment",
                    entitySaved!!.caTitle)
        }

    }

    @Test
    fun givenExistingClazzAssignment_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {

        val clazzAssignment = ClazzAssignment().apply {
            caTitle = "test Assignment"
            caStartDate = DateTime(2020, 10, 10).unixMillisLong
            caClazzUid = testClazz.clazzUid
            caUid = repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to clazzAssignment.caUid.toString())

        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)
        val presenter = ClazzAssignmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)
        whenever(mockView.startDate).thenReturn(DateTime(2021,5,10).unixMillisLong)
        whenever(mockView.deadlineDate).thenReturn(Long.MAX_VALUE)
        whenever(mockView.gracePeriodDate).thenReturn(Long.MAX_VALUE)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.caTitle = "New Spelling Clazz"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            repo.waitUntilAsyncOrTimeout(5000, listOf("ClazzAssignment")) {
                repo.clazzAssignmentDao.findByUidAsync(clazzAssignment.caUid)?.caTitle == "New Spelling Clazz"
            }

            val entitySaved = repo.clazzAssignmentDao.findByUidAsync(clazzAssignment.caUid)

            Assert.assertEquals("Name was saved and updated",
                    "New Spelling Clazz", entitySaved!!.caTitle)
        }


    }


}
