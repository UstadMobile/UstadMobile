package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.GetStartedView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.SiteEnterLinkView
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.IOException

class GetStartedPresenterTest {

    private lateinit var mockedView: GetStartedView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var mPresenter: GetStartedPresenter

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

        mPresenter = GetStartedPresenter(context, mapOf(),mockedView, di)
    }

    @Test
    fun givenGetStartedOptions_whenUsePublicLibraryClicked_thenShouldGoToLoginScreen() {
        whenever(impl.getAppPref(eq(OnBoardingView.PREF_TAG), any(), any())).thenReturn("true")
        mPresenter.onCreate(null)
        mPresenter.goToPublicLibrary()
        argumentCaptor<String>{
            verify(impl).go(capture(), any(), any())
            Assert.assertEquals("Login screen was opened",
                    Login2View.VIEW_NAME, firstValue)
        }
    }

    @Test
    fun givenGetStartedOptions_whenJoinExistingWorkSpaceIsClicked_thenShouldAllowToEnterWorkSpaceLink() {
        mPresenter.onCreate(null)
        mPresenter.joinExistingWorkSpace()
        argumentCaptor<String>{
            verify(impl).go(capture(), any(), any())
            Assert.assertEquals("Enter link screen was opened",
                    SiteEnterLinkView.VIEW_NAME, firstValue)
        }
    }

    @Test
    fun givenGetStartedOptions_whenCreateNewWorkSpaceIsClicked_thenShouldOpenABrowserWindow() {
        mPresenter.onCreate(null)
        mPresenter.createNewWorkSpace()
        verify(mockedView).createNewWorkSpace()
    }

}