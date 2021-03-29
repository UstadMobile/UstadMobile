package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import org.junit.Assert
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

    @Before
    @Throws(IOException::class)
    fun setup() {
        mockedView = mock()
        impl = mock()

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
        }
    }

    @Test
    fun givenAppLaunched_whenUserHasNotLoggedInBefore_thenShouldNavigateToGetStarted() {
        whenever(impl.getAppConfigBoolean(eq(AppConfig.KEY_ALLOW_SERVER_SELECTION), any())).thenReturn(true)
        whenever(impl.getAppPref(eq(Login2Presenter.PREFKEY_USER_LOGGED_IN), eq("false"), any())).thenReturn("false")
        mPresenter = RedirectPresenter(context, mapOf(),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(mockedView).showNextScreen(eq(SiteEnterLinkView.VIEW_NAME), any())
    }

    @Test
    fun givenAppLaunched_whenUserHasLoggedInBefore_thenShouldNavigateContentList() {
        whenever(impl.getAppPref(eq(Login2Presenter.PREFKEY_USER_LOGGED_IN), eq("false"), any())).thenReturn("true")
        mPresenter = RedirectPresenter(context, mapOf(),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(mockedView).showNextScreen(eq(ContentEntryListTabsView.VIEW_NAME), any())
    }

    @Test
    fun givenNextArgProvied_whenOnCreateCalled_thenShouldGoToNextDest() {
        mPresenter = RedirectPresenter(context, mapOf(ARG_NEXT to "${ContentEntry2DetailView.VIEW_NAME}?entityUid=42"),
                mockedView, di)
        mPresenter.onCreate(null)
        verify(mockedView).showNextScreen(eq(ContentEntry2DetailView.VIEW_NAME),
            eq(mapOf("entityUid" to "42")))
    }

}