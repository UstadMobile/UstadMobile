package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.core.view.SplashView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.XapiPackageContentView
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

class SplashScreenPresenterTest {

    private lateinit var view: SplashView

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var presenter: SplashPresenter

    private val context = Any()

    @Before
    @Throws(IOException::class)
    fun setup() {
        view = mock()
        impl = mock()

        presenter = SplashPresenter(context, mapOf(),view,impl)
    }

    @Test
    fun givenAppPrefAnimateOrganisationIconSetToTrue_whenAppStarted_shouldAnimateTheIcon() {
        doAnswer {
            "false"
        }.`when`(impl).getAppPref(any(), any(), any())

        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view).animateOrganisationIcon(eq(true), eq(true))
    }

    @Test
    fun givenAppPrefAnimateOrganisationIconSetToFalse_whenAppStarted_shouldNotAnimateTheIcon() {
        doAnswer {
            "false"
        }.`when`(impl).getAppPref(any(), any(), any())

        doAnswer {
            "false"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view).animateOrganisationIcon(eq(false), eq(true))
    }


    @Test
    fun givenAppIsLaunchedForTheFirstTime_whenLibPreloadIsSetToTrue_shouldPreloadLibs() {
        doAnswer {
            "false"
        }.`when`(impl).getAppPref(any(), any(), any())

        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view).preloadData()
    }

    @Test
    fun givenAppWasLaunchedPreviously_whenLibPreloadIsSetToTrue_shouldNoPreloadLibs() {
        doAnswer {
            "true"
        }.`when`(impl).getAppPref(any(), any(), any())

        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view, times(0)).preloadData()
    }

}