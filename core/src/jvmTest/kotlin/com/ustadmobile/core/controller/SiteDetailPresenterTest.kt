
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.SiteDetailView
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
import com.ustadmobile.lib.db.entities.Site
import org.kodein.di.DI

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class SiteDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: SiteDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoSiteDaoSpy: SiteDao

    private lateinit var repoSiteTermsDaoSpy: SiteTermsDao

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

        repoSiteDaoSpy = spy(repo.siteDao)
        whenever(repo.siteDao).thenReturn(repoSiteDaoSpy)

        repoSiteTermsDaoSpy = spy(repo.siteTermsDao)
        whenever(repo.siteTermsDao).thenReturn(repoSiteTermsDaoSpy)
    }

    @Test
    fun givenWorkspaceExists_whenOnCreateCalled_thenWorkspaceIsSetOnView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()


        val testEntity = Site().apply {
            //set variables here
            this.siteUid = repo.siteDao.insert(this)
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.siteUid.toString())
        val presenter = SiteDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.siteUid, entityValSet.siteUid)

        verify(repoSiteTermsDaoSpy).findAllTermsAsFactory()
    }



}