/*

package com.ustadmobile.core.controller


import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.ClazzAssignment


import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import org.kodein.di.DI


*/
/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 *//*

class ClazzAssignmentEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ClazzAssignmentEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzAssignmentDaoSpy: ClazzAssignmentDao

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

        repoClazzAssignmentDaoSpy = spy(repo.clazzAssignmentDao)
        whenever(repo.clazzAssignmentDao).thenReturn(repoClazzAssignmentDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val presenterArgs = mapOf<String, String>()

        val presenter = ClazzAssignmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        //e.g. initialEntity.someNameField = "Bob"

        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = clientDbRule.db.clazzAssignmentDao.findAllLive()

        //TODO: wait until the presenter has saved the entity e.g.
        */
/*
        runBlocking {
            db.waitUntil(5000, listOf("@Entity")) {
                db.clazzAssignmentDao.findBySomeCondition()?.someField == initialEntity.someField
            }
        }

        val entitySaved = db.clazzAssignmentDao.findBySomeCondition()
        Assert.assertEquals("Entity was saved to database", "Bob",
                entitySaved.someNameField)
        *//*

    }

    @Test
    fun givenExistingClazzAssignment_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = ClazzAssignment().apply {
            someName = "Spelling Clazz"
            caUid = clientDbRule.repo.clazzAssignmentDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.caUid.toString())
        val presenter = ClazzAssignmentEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        //e.g. initialEntity!!.someName = "New Spelling Clazz"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("ClazzAssignment")) {
                db.clazzAssignmentDao.findByUid(testEntity.caUid)?.someName == "NewSpelling Clazz"
            }
        }

        val entitySaved = db.clazzAssignmentDao.findByUid(testEntity.caUid)

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.someName)
    }


}*/
