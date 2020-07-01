package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.GetStartedView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.WorkspaceEnterLinkView
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException

class GetStartedPresenterTest {

    private lateinit var mockedView: GetStartedView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var mPresenter: GetStartedPresenter

    private val context = Any()

    @Before
    @Throws(IOException::class)
    fun setup() {
        mockedView = mock()
        impl = mock()

        mPresenter = GetStartedPresenter(context, mapOf(),mockedView,impl)
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
                    WorkspaceEnterLinkView.VIEW_NAME, firstValue)
        }
    }

    @Test
    fun givenGetStartedOptions_whenCreateNewWorkSpaceIsClicked_thenShouldOpenABrowserWindow() {
        mPresenter.onCreate(null)
        mPresenter.createNewWorkSpace()
        verify(mockedView).createNewWorkSpace()
    }

}