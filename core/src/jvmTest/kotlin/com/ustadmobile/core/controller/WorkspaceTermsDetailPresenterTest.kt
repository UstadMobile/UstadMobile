
package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.WorkspaceTermsDetailView
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.SiteTermsDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorLifecycleObserver

import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.SiteTerms
import kotlinx.coroutines.runBlocking
import org.kodein.di.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class WorkspaceTermsDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: WorkspaceTermsDetailView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoWorkspaceTermsDaoSpy: SiteTermsDao

    private lateinit var di: DI

    private lateinit var mockSystemImpl: UstadMobileSystemImpl

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        mockSystemImpl = mock { }

        di = DI {
            import(ustadTestRule.diModule)

            bind<UstadMobileSystemImpl>(overrides = true) with singleton { mockSystemImpl }
        }

        val repo: UmAppDatabase by di.activeRepoInstance()


        repoWorkspaceTermsDaoSpy = spy(repo.siteTermsDao)
        whenever(repo.siteTermsDao).thenReturn(repoWorkspaceTermsDaoSpy)

        //TODO: insert any entities required for all tests
    }

    @Test
    fun givenWorkspaceTermsExists_whenOnCreateCalled_thenWorkspaceTermsIsSetOnView() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = SiteTerms().apply {
            //set variables here
            sTermsUid = runBlocking { repo.siteTermsDao.insertAsync(this@apply) }
        }

        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.sTermsUid.toString())

        val presenter = WorkspaceTermsDetailPresenter(context, presenterArgs, mockView,
                mockLifecycleOwner, di)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.sTermsUid, entityValSet.wtUid)
    }

    @Test
    fun givenTermsExistForLang_whenOnCreateCalled_thenShouldShowTermsInSpecifiedLang() {
        val systemImpl: UstadMobileSystemImpl = di.direct.instance()

        systemImpl.stub {
            on { getDisplayedLocale(any()) }.thenReturn("fa")
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = SiteTerms().apply {
            //set variables here
            termsHtml = "Salam"
            wtLang = "fa"
            sTermsUid = runBlocking { repo.siteTermsDao.insertAsync(this@apply) }
        }

        val presenter = WorkspaceTermsDetailPresenter(Any(),
                mapOf(UstadView.ARG_SERVER_URL to "http://localhost/",
                    WorkspaceTermsDetailView.ARG_USE_DISPLAY_LOCALE to true.toString(),
                    WorkspaceTermsDetailView.ARG_SHOW_ACCEPT_BUTTON to true.toString()),
                mockView, mockLifecycleOwner, di)

        presenter.onCreate(null)

        verify(mockView, timeout(1000).atLeastOnce()).entity = argForWhich {
            termsHtml == "Salam"
        }

        verifyBlocking(repoWorkspaceTermsDaoSpy, timeout(1000)) {
            findSiteTerms("fa")
        }

        verify(mockView, timeout(1000)).acceptButtonVisible = true
    }


}