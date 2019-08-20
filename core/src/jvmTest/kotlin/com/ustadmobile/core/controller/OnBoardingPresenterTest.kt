package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

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

        doAnswer {
            "en-US,fa-AF,ps-AF,ar-AE"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        doAnswer {
            "true"
        }.`when`(impl).getAppPref(any(), any(), any())

        doAnswer {
            UstadMobileConstants.LANGUAGE_NAMES
        }.`when`(impl).getAllUiLanguage(any())

        doAnswer {
            "en"
        }.`when`(impl).getDisplayedLocale(any())


        presenter = OnBoardingPresenter(context, mapOf(),view,impl)
    }

    @Test
    fun givenListOfUiSupportedLanguages_whenAppStarts_shouldBeDisplayed() {
        presenter.onCreate(mapOf())
        verify(view).setLanguageOptions(any())
    }

    @Test
    fun givenListOfUiSupportedLanguages_whenDifferentDisplayLanguageIsSelected_shouldSetDefaultLocale() {
        presenter.onCreate(mapOf())

        presenter.handleLanguageSelected(1)

        verify(impl).setLocale("en-US", context)
    }


    @Test
    fun givenListOfUiSupportedLanguages_whenDisplayedLanguageIsSelected_shouldNotSetDefaultLocale() {
        presenter.onCreate(mapOf())

        presenter.handleLanguageSelected(0)

        verify(impl, times(0)).setLocale("en", context)
    }

}