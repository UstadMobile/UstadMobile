
package com.ustadmobile.core.controller

import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import kotlinx.coroutines.runBlocking
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
class ClazzEdit2PresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ClazzEdit2View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzDaoSpy: ClazzDao

    private lateinit var di: DI

    private lateinit var testNavController: UstadNavController

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        val mockClazzLogCreator = mock<ClazzLogCreatorManager>()

        di = DI {
            import(ustadTestRule.diModule)
            bind<ClazzLogCreatorManager>() with scoped(ustadTestRule.endpointScope!!).singleton { mockClazzLogCreator }
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

        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = db.clazzDao.findAllLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "Bob",
                entitySaved.clazzName)
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

        val presenter = ClazzEdit2Presenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.clazzName = "New Spelling Clazz"
        initialEntity.clazzStartTime = DateTime(2020, 10, 10).unixMillisLong

        presenter.handleClickSave(initialEntity)

        val entitySaved = runBlocking {
            db.clazzDao.findByUidLive(testEntity.clazzUid)
                    .waitUntil(5000) { it?.clazzName == "New Spelling Clazz" }.getValue()
        }

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.clazzName)
    }


}