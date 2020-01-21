package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LoginView

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
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

        presenter = UserProfilePresenter(context, mapOf(),view,UmAppDatabase.getInstance(context),impl)

        UmAccountManager.setActiveAccount(UmAccount(11,"username",
                "",""), context)
    }


    @Test
    fun givenUserIsLoggedIn_WhenClickedLogout_thenShouldLogout(){

        presenter.onCreate(mapOf())

        presenter.handleUserLogout()

        val firstDest = impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, "BasePoint", context)

        verify(impl).go(eq(LoginView.VIEW_NAME), any(), any())

    }


    @Test
    fun givenUserIsLoggedIn_WhenClickedLanguageOption_thenShouldShowTheOptions(){

        presenter.onCreate(mapOf())

        presenter.handleShowLanguageOptions()

        verify(view).setLanguageOption(any())
    }

}
