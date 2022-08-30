
package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import org.mockito.kotlin.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ClazzEdit2PresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzEdit2View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoClazzDaoSpy: ClazzDao

    private lateinit var di: DI

    private lateinit var testNavController: UstadNavController

    @Before
    fun setup() {
        mockView = mockEditView { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        val mockClazzLogCreator = mock<ClazzLogCreatorManager>()

        di = DI {
            import(ustadTestRule.diModule)
            bind<ClazzLogCreatorManager>() with singleton { mockClazzLogCreator }
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoClazzDaoSpy = spy(repo.clazzDao)
        whenever(repo.clazzDao).thenReturn(repoClazzDaoSpy)

        testNavController = di.direct.instance()
    }


    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val presenterArgs = mapOf<String, String>()

        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        testNavController.navigate(ClazzEdit2View.VIEW_NAME, presenterArgs)
        val presenter = ClazzEdit2Presenter(context, presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        initialEntity.clazzName = "Bob"
        initialEntity.clazzStartTime = DateTime(2020, 10, 10).unixMillisLong

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = db.clazzDao.findAllLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "Bob",
                entitySaved.clazzName)

        //Verify that the appropriate ScopedGrants and groups have been created
        val clazzScopedGrants = runBlocking {
            db.scopedGrantDao.findByTableIdAndEntityUid(Clazz.TABLE_ID, entitySaved.clazzUid)
        }
        val teacherGroup = db.personGroupDao.findByUid(entitySaved.clazzTeachersPersonGroupUid)
        Assert.assertNotNull("Teacher group exists", teacherGroup)
        val teacherGrant = clazzScopedGrants.find {
            it.scopedGrant?.sgGroupUid == entitySaved.clazzTeachersPersonGroupUid
        }
        Assert.assertEquals("Teacher grant has default teacher permissions",
            Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT, teacherGrant?.scopedGrant?.sgPermissions)

    }

    @Test
    fun givenExistingClazz_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()


        val testEntity = Clazz().apply {
            clazzName = "Spelling Clazz"
            clazzUid = repo.clazzDao.insert(this)
            clazzStartTime = DateTime(2020, 10, 10).unixMillisLong
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())
        testNavController.navigate(ClazzEdit2View.VIEW_NAME, presenterArgs)

        val presenter = ClazzEdit2Presenter(context, presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.clazzName = "New Spelling Clazz"
        initialEntity.clazzStartTime = DateTime(2020, 10, 10).unixMillisLong

        mockView.verifyFieldsEnabled()
        presenter.handleClickSave(initialEntity)

        val entitySaved = runBlocking {
            db.clazzDao.findByUidLive(testEntity.clazzUid)
                    .waitUntil(5000) { it?.clazzName == "New Spelling Clazz" }.getValue()
        }

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.clazzName)
    }


}