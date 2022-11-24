package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import org.mockito.kotlin.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.IOException

class OnBoardingPresenterTest {

    private lateinit var view: OnBoardingView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var presenter: OnBoardingPresenter

    private val context = Any()

    private lateinit var di : DI

    @Before
    @Throws(IOException::class)
    fun setup() {
        view = mock()
        impl = mock()

        whenever(impl.getAllUiLanguagesList()).thenReturn(
            listOf(
                UstadMobileSystemCommon.UiLanguage("", "sys default"),
                UstadMobileSystemCommon.UiLanguage("en", "English"),
                UstadMobileSystemCommon.UiLanguage("ar", "Arabic")))

        whenever(impl.getLocale()).thenReturn("")

        di  = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
        }

        presenter = OnBoardingPresenter(context, mapOf(),view, di)
    }

    @Test
    fun givenListOfUiSupportedLanguages_whenAppStarts_shouldBeDisplayed() {

        presenter.onCreate(mapOf())
        verify(view).setLanguageOptions(eq(listOf("sys default", "English", "Arabic")),eq("sys default"))
    }

    @Test
    fun givenListOfUiSupportedLanguages_whenDifferentDisplayLanguageIsSelected_shouldSetDefaultLocale() {
        presenter.onCreate(mapOf())
        presenter.handleLanguageSelected(2)
        verify(impl).setLocale("ar")
    }

    @Test
    fun givenListOfUiSupportedLangauges_whenUserSelectsDifferentLanguage_thenShouldRestart() {
        whenever(impl.getDisplayedLocale()).thenReturn("en")

        presenter.onCreate(mapOf())
        presenter.handleLanguageSelected(2)

        verify(impl).setLocale("ar")
        verify(view).restartUI()
    }


    @Test
    fun givenListOfUiSupportedLangauges_whenUserSelectsSameLangAsDisplayed_thenShouldNotRestart() {
        whenever(impl.getDisplayedLocale()).thenReturn("ar")

        presenter.onCreate(mapOf())
        presenter.handleLanguageSelected(2)

        verify(view, times(0)).restartUI()
    }

}