package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import org.junit.Before
import org.junit.Test
import java.io.IOException

class OnBoardingPresenterTest {

    private lateinit var view: OnBoardingView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var presenter: OnBoardingPresenter

    private val context = Any()

    @Before
    @Throws(IOException::class)
    fun setup() {
        view = mock()
        impl = mock()

        whenever(impl.getAllUiLanguagesList(any())).thenReturn(listOf("" to "sys default", "en" to "English",
                "ar" to "Arabic"))

        presenter = OnBoardingPresenter(context, mapOf(),view,impl)
    }

    @Test
    fun givenListOfUiSupportedLanguages_whenAppStarts_shouldBeDisplayed() {
        presenter.onCreate(mapOf())
        verify(view).setLanguageOptions(eq(listOf("sys default", "English", "Arabic")))
    }

    @Test
    fun givenListOfUiSupportedLanguages_whenDifferentDisplayLanguageIsSelected_shouldSetDefaultLocale() {
        presenter.onCreate(mapOf())

        presenter.handleLanguageSelected(2)
        verify(impl).setLocale("ar", context)
    }

    @Test
    fun givenListOfUiSupportedLangauges_whenUserSelectsDifferentLanguage_thenShouldRestart() {
        whenever(impl.getDisplayedLocale(any())).thenReturn("en")

        presenter.onCreate(mapOf())
        presenter.handleLanguageSelected(2)

        verify(impl).setLocale("ar", context)
        verify(view).restartUI()
    }


    @Test
    fun givenListOfUiSupportedLangauges_whenUserSelectsSameLangAsDisplayed_thenShouldNotRestart() {
        whenever(impl.getDisplayedLocale(any())).thenReturn("ar")

        presenter.onCreate(mapOf())
        presenter.handleLanguageSelected(2)

        verify(view, times(0)).restartUI()
    }

}