package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
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

        screenPresenter = SplashScreenPresenter(context, mapOf(),view,impl)
    }

    @Test
    fun givenAppPrefAnimateOrganisationIconSetToTrue_whenAppStarted_shouldAnimateTheIcon() {
        doAnswer {
            "false"
        }.`when`(impl).getAppPref(any(), any(), any())

        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        screenPresenter.onCreate(mapOf())

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

        screenPresenter.onCreate(mapOf())

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

        screenPresenter.onCreate(mapOf())
    }

    @Test
    fun givenAppWasLaunchedPreviously_whenLibPreloadIsSetToTrue_shouldNoPreloadLibs() {
        doAnswer {
            "true"
        }.`when`(impl).getAppPref(any(), any(), any())

        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        screenPresenter.onCreate(mapOf())
    }

}