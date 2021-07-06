package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import org.mockito.kotlin.*
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.IOException

class RedirectPresenterTest {

    private lateinit var mockedView: RedirectView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var mPresenter: RedirectPresenter

    private val context = Any()

    private lateinit var di: DI

    private lateinit var mockedAccountManager: UstadAccountManager

    @Before
    fun setup() {
        mockedView = mock()
        impl = mock()

        mockedAccountManager = mock { }

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
            bind<UstadAccountManager>() with singleton { mockedAccountManager }
        }
    }

    @Test
    fun givenAppLaunched_whenUserHasNotLoggedInBefore_thenShouldNavigateToGetStarted() {
        whenever(impl.getAppConfigBoolean(eq(AppConfig.KEY_ALLOW_SERVER_SELECTION), any())).thenReturn(true)
        mockedAccountManager.stub {
            onBlocking { activeSessionCount(any()) }.thenReturn(0)
        }

        mPresenter = RedirectPresenter(context, mapOf(),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(impl, timeout(5000)).goToViewLink(eq(SiteEnterLinkView.VIEW_NAME), any(), any())
    }

    @Test
    fun givenAppLaunched_whenUserHasLoggedInBefore_thenShouldNavigateFeedList() {
        mockedAccountManager.stub {
            onBlocking { activeSessionCount(any()) }.thenReturn(1)
        }

        mPresenter = RedirectPresenter(context, mapOf(),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(impl, timeout(5000)).goToViewLink(eq(ContentEntryListTabsView.VIEW_NAME), any(), any())
    }

    @Test
    fun givenNextArgProvied_whenOnCreateCalled_thenShouldGoToNextDest() {
        val viewLink = "${ContentEntryDetailView.VIEW_NAME}?entityUid=42"
        mockedAccountManager.stub {
            onBlocking { activeSessionCount(any()) }.thenReturn(1)
        }

        mPresenter = RedirectPresenter(context, mapOf(ARG_NEXT to viewLink),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(impl, timeout(5000)).goToViewLink(eq(viewLink), any(), any())
    }

}