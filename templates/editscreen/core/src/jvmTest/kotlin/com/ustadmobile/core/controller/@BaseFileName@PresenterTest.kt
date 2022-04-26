
package com.ustadmobile.core.controller


import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.@BaseFileName@View
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.@Entity@Dao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.@Entity@
@EditEntity_Import@

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import org.kodein.di.DI
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.mockEditView
import com.ustadmobile.core.util.verifyFieldsEnabled



/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class @BaseFileName@PresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: @BaseFileName@View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repo@Entity@DaoSpy: @Entity@Dao

    @Before
    fun setup() {
        mockView = mockEditView { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repo@Entity@DaoSpy = spy(repo.@Entity_VariableName@Dao)
        whenever(repo.@Entity_VariableName@Dao).thenReturn(repo@Entity@DaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val presenterArgs = mapOf<String, String>()

        val presenter = @BaseFileName@Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        //e.g. initialEntity.someNameField = "Bob"

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = clientDbRule.db.@Entity_VariableName@Dao.findAllLive()

        //TODO: wait until the presenter has saved the entity e.g.
        /*
        runBlocking {
            db.waitUntil(5000, listOf("@Entity")) {
                db.@Entity_VariableName@Dao.findBySomeCondition()?.someField == initialEntity.someField
            }
        }

        val entitySaved = db.@Entity_VariableName@Dao.findBySomeCondition()
        Assert.assertEquals("Entity was saved to database", "Bob",
                entitySaved.someNameField)
        */
    }

    @Test
    fun givenExisting@Entity@_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = @Entity@().apply {
            someName = "Spelling Clazz"
            @Entity_VariableName@Uid = clientDbRule.repo.@Entity_VariableName@Dao.insert(this)
        }
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.@Entity_VariableName@Uid.toString())
        val presenter = @BaseFileName@Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        //e.g. initialEntity!!.someName = "New Spelling Clazz"

        mockView.verfiyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("@Entity@")) {
                db.@Entity_VariableName@Dao.findByUid(testEntity.@Entity_VariableName@Uid)?.someName == "NewSpelling Clazz"
            }
        }

        val entitySaved = db.@Entity_VariableName@Dao.findByUid(testEntity.@Entity_VariableName@Uid)

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.someName)
    }


}