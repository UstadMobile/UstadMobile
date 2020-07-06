
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ClazzDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.db.waitUntil
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ClazzEdit2PresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ClazzEdit2View

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoClazzDaoSpy: ClazzDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoClazzDaoSpy = spy(clientDbRule.db.clazzDao)
        whenever(clientDbRule.db.clazzDao).thenReturn(repoClazzDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
        val presenterArgs = mapOf<String, String>()

        val presenter = ClazzEdit2Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        initialEntity.clazzName = "Bob"

        presenter.handleClickSave(initialEntity)

        val existingEntitiesLive = clientDbRule.db.clazzDao.findAllLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "Bob",
                entitySaved.clazzName)
    }

    @Test
    fun givenExistingClazz_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = Clazz().apply {
            clazzName = "Spelling Clazz"
            clazzUid = clientDbRule.repo.clazzDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.clazzUid.toString())
        val presenter = ClazzEdit2Presenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.clazzName = "New Spelling Clazz"

        presenter.handleClickSave(initialEntity)

        val entitySaved = runBlocking {
            clientDbRule.db.clazzDao.findByUidLive(testEntity.clazzUid)
                    .waitUntil(5000) { it?.clazzName == "New Spelling Clazz" }.getValue()
        }

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.clazzName)
    }


}