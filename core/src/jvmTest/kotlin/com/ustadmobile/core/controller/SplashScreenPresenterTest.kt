package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.SplashScreenView
import org.junit.Before
import org.junit.Test
import java.io.IOException

class SplashScreenPresenterTest {

    private lateinit var view: SplashScreenView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var screenPresenter: SplashScreenPresenter

    private val context = Any()

    @Before
    @Throws(IOException::class)
    fun setup() {
        view = mock()
        impl = mock()

        screenPresenter = SplashScreenPresenter(context, mapOf(),view,impl, delay = 100)
    }

    @Test
    fun givenOnboardingShown_whenOnCreateCalled_thenShouldCallStartUi() {
        whenever(impl.getAppPref(eq(OnBoardingView.PREF_TAG), any(), any())).thenReturn("true")
        screenPresenter.onCreate(null)
        verify(impl, timeout(5000)).startUI(any())
    }

    @Test
    fun givenOnboardingNotShown_whenOnCreateCalled_thenShouldGoToOnboarding() {
        whenever(impl.getAppPref(eq(OnBoardingView.PREF_TAG), any(), any())).thenReturn("false")
        screenPresenter.onCreate(null)
        verify(impl, timeout(5000)).go(eq(OnBoardingView.VIEW_NAME), any(), any())
    }

}