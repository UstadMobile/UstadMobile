
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
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
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

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ClazzAssignmentEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

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

        val repo: UmAppDatabase by di.activeRepoInstance()
        testNavController = di.direct.instance()

        repoClazzAssignmentDaoSpy = spy(repo.clazzAssignmentDao)
        whenever(repo.clazzAssignmentDao).thenReturn(repoClazzAssignmentDaoSpy)

        testClazz = Clazz().apply {
            clazzName = "Spelling Clazz"
            clazzUid = repo.clazzDao.insert(this)
            clazzStartTime = DateTime(2020, 8, 10).unixMillisLong
        }


    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {

        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val newAssignment = ClazzAssignment().apply {
            caClazzUid = testClazz.clazzUid
        }
        val jsonStr = Gson().toJson(newAssignment)

        val presenterArgs = mutableMapOf<String, String>()
        presenterArgs[ARG_ENTITY_JSON] = jsonStr
        testNavController.navigate(ClazzAssignmentEditView.VIEW_NAME, presenterArgs)
        val presenter = ClazzAssignmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        initialEntity.caTitle = "Test Clazz Assignment"
        initialEntity.caStartDate = DateTime(2021,5,10).unixMillisLong

        presenter.handleClickSave(initialEntity)

        verify(systemImpl, timeout(5000)).go(
                eq(ClazzAssignmentDetailView.VIEW_NAME),
                any(), any(), any())

        //TODO: wait until the presenter has saved the entity e.g.
        runBlocking {
            db.waitUntil(5000, listOf("ClazzAssignment")) {
                db.clazzAssignmentDao.findClazzAssignment()?.caTitle == "Test Clazz Assignment"
            }
        }

        val entitySaved = db.clazzAssignmentDao.findClazzAssignment()
        Assert.assertEquals("Entity was saved to database", "Test Clazz Assignment",
                entitySaved!!.caTitle)


    }

    @Test
    fun givenExistingClazzAssignment_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {

        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()


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

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.caTitle = "New Spelling Clazz"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("ClazzAssignment")) {
                runBlocking {
                    db.clazzAssignmentDao.findByUidAsync(clazzAssignment.caUid)?.caTitle == "New Spelling Clazz"
                }
            }

            val entitySaved = db.clazzAssignmentDao.findByUidAsync(clazzAssignment.caUid)

            Assert.assertEquals("Name was saved and updated",
                    "New Spelling Clazz", entitySaved!!.caTitle)
        }


    }


}
