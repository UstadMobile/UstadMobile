package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
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
    fun givenAppLaunched_whenNavigateFromOnBoardingScreen_thenShouldNavigateToGetStarted() {
        mPresenter = RedirectPresenter(context, mapOf(UstadView.ARG_FROM to OnBoardingView.VIEW_NAME),
                mockedView, di)
        mPresenter.onCreate(null)
        argumentCaptor<Boolean>{
            verify(mockedView).showGetStarted = capture()
            Assert.assertEquals("Get started screen was opened", true, firstValue)
        }
    }

    @Test
    fun givenAppLaunched_whenNavigateFromOtherScreens_thenShouldNavigateContentList() {
        mPresenter = RedirectPresenter(context, mapOf(),
                mockedView, di)
        mPresenter.onCreate(null)
        argumentCaptor<Boolean>{
            verify(mockedView).showGetStarted = capture()
            Assert.assertEquals("Content entry list  screen was opened",
                    false, firstValue)
        }
    }

}