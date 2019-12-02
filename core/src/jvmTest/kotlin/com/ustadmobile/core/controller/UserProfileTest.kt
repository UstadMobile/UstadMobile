package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SplashScreenView
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.lib.db.entities.UmAccount
import org.junit.Before
import org.junit.Test


class UserProfileTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: UserProfileView

    private lateinit var presenter:UserProfilePresenter

    private val context = Any()

    @Before
    fun setUp(){
        view = mock()
        impl = mock {
            on{getAppConfigString(any(), any(), any())}.thenAnswer{
                "en-US,fa-AF,ps-AF,ar-AE"
            }

            on{getAllUiLanguage(any())}.thenAnswer{
                UstadMobileConstants.LANGUAGE_NAMES
            }

            on{getDisplayedLocale(any())}.thenAnswer{
                "en"
            }
        }

        presenter = UserProfilePresenter(context, mapOf(),view,UmAppDatabase.getInstance(context).personDao,impl)

        UmAccountManager.setActiveAccount(UmAccount(11,"username",
                null,null), context)
    }


    @Test
    fun givenUserIsLoggedIn_WhenClickedLogout_thenShouldLogout(){

        presenter.onCreate(mapOf())

        presenter.handleUserLogout()

        verify(impl).go(eq(SplashScreenView.VIEW_NAME), any())
    }


    @Test
    fun givenUserIsLoggedIn_WhenClickedLanguageOption_thenShouldShowTheOptions(){

        presenter.onCreate(mapOf())

        presenter.handleShowLanguageOptions()

        verify(view).setLanguageOption(any())
    }

}
