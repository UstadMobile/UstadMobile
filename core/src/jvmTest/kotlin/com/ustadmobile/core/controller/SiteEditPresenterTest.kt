
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.SiteEditView
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.db.dao.SiteDao
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.lifecycle.LifecycleObserver

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.lib.db.entities.Site
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class SiteEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: SiteEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoWorkSpaceDaoSpy: SiteDao

    private lateinit var testNavController: UstadNavController

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoWorkSpaceDaoSpy = spy(repo.siteDao)
        whenever(repo.siteDao).thenReturn(repoWorkSpaceDaoSpy)

        testNavController = di.direct.instance()

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenExistingWorkSpace_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()

        val testEntity = Site().apply {
            siteName = "Spelling Clazz"
            siteUid = repo.siteDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.siteUid.toString())
        testNavController.navigate(SiteEditView.VIEW_NAME, presenterArgs)
        val presenter = SiteEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.siteName = "New Spelling Clazz"

        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("Site")) {
                db.siteDao.getSite()?.siteName == "New Spelling Clazz"
            }
        }

        val entitySaved = db.siteDao.getSite()

        Assert.assertEquals("Name was saved and updated",
                "New Spelling Clazz", entitySaved!!.siteName)
    }


}