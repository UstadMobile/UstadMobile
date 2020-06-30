package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AccountGetStartedView
import com.ustadmobile.core.view.OnBoardingView
import org.junit.Before
import org.junit.Test
import java.io.IOException

class GetStartedPresenterTest {

    private lateinit var mockedView: AccountGetStartedView

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
        //verify(impl).go(any())
    }

    @Test
    fun givenGetStartedOptions_whenJoinExistingWorkSpaceIsClicked_thenShouldAllowToEnterWorkSpaceLink() {
        mPresenter.onCreate(null)
        mPresenter.joinExistingWorkSpace()
    }

    @Test
    fun givenGetStartedOptions_whenCreateNewWorkSpaceIsClicked_thenShouldOpenABrowserWindow() {
        mPresenter.onCreate(null)
        mPresenter.createNewWorkSpace()
        verify(mockedView).createNewWorkSpace()
    }

}