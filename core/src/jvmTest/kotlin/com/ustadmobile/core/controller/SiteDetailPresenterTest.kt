
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.SiteDetailView
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SiteTermsDao
import com.ustadmobile.core.db.dao.SiteDao
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.util.*

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.door.lifecycle.DoorState
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

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoSiteDaoSpy: SiteDao

    private lateinit var repoSiteTermsDaoSpy: SiteTermsDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
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

        //Note: the initial site entity itself is created by UstadTestRule
        val siteUid = db.siteDao.getSite()?.siteUid ?: throw IllegalStateException("No site in db!")
        val presenterArgs = mapOf(ARG_ENTITY_UID to siteUid.toString())
        val presenter = SiteDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                siteUid, entityValSet.siteUid)

        verify(repoSiteTermsDaoSpy).findAllTermsAsFactory()
    }

}