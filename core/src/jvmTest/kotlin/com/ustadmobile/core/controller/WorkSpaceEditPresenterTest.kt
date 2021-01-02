
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.WorkSpaceEditView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.SiteDao
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.WorkSpace

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import org.kodein.di.DI

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class WorkSpaceEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: WorkSpaceEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoWorkSpaceDaoSpy: SiteDao

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

        repoWorkSpaceDaoSpy = spy(repo.siteDao)
        whenever(repo.siteDao).thenReturn(repoWorkSpaceDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenExistingWorkSpace_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()

        val testEntity = WorkSpace().apply {
            name = "Spelling Clazz"
            uid = repo.siteDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.uid.toString())
        val presenter = WorkSpaceEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.name = "New Spelling Clazz"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("WorkSpace")) {
                db.siteDao.getSite()?.name == "New Spelling Clazz"
            }
        }

        val entitySaved = db.siteDao.getSite()

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.name)
    }


}