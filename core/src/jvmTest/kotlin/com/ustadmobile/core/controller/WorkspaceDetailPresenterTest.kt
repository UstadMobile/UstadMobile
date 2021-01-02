
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.WorkspaceDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.SiteTermsDao
import com.ustadmobile.core.db.dao.SiteDao
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorLifecycleObserver

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.lib.db.entities.WorkSpace
import org.kodein.di.DI

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class WorkspaceDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: WorkspaceDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoWorkspaceDaoSpy: SiteDao

    private lateinit var repoWorkspaceTermsDaoSpy: SiteTermsDao

    private lateinit var di: DI

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

        repoWorkspaceDaoSpy = spy(repo.siteDao)
        whenever(repo.siteDao).thenReturn(repoWorkspaceDaoSpy)

        repoWorkspaceTermsDaoSpy = spy(repo.siteTermsDao)
        whenever(repo.siteTermsDao).thenReturn(repoWorkspaceTermsDaoSpy)
    }

    @Test
    fun givenWorkspaceExists_whenOnCreateCalled_thenWorkspaceIsSetOnView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()


        val testEntity = WorkSpace().apply {
            //set variables here
            this.uid = repo.siteDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.uid.toString())
        val presenter = WorkspaceDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.uid, entityValSet.uid)

        verify(repoWorkspaceTermsDaoSpy).findAllTermsAsFactory()
    }


    /*
    @Test
    fun givenWorkspaceExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()


        val testEntity = WorkSpace().apply {
            //set variables here
            this.uid = repo.workSpaceDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.uid.toString())
        val presenter = WorkspaceDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue()

        presenter.handleClickEdit()

        val systemImpl: UstadMobileSystemImpl by di.instance()

        verify(systemImpl, timeout(5000)).go(eq(WorkspaceEditView.VIEW_NAME),
            eq(mapOf(ARG_ENTITY_UID to testEntity.workspaceUid.toString())), any())
    }
    */


}