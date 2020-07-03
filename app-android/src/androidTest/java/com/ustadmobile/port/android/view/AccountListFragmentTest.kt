package com.ustadmobile.port.android.view

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.nhaarman.mockitokotlin2.*
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.controller.AccountListPresenter
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.core.view.GetStartedView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.hasInputLayoutError
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.hamcrest.Matchers.not
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Account List Tests")
@ExperimentalStdlibApi
class AccountListFragmentTest {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun givenStoreAccounts_whenLaunched_thenShouldShowAllAccounts(){

    }

    @Test
    fun givenActiveAccountExists_whenLaunched_thenShouldShowIt(){

    }

    @Test
    fun givenAddAccountButton_whenClicked_thenShouldOpenGetStarted(){

    }


    @Test
    fun givenDeleteAccountButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){

    }

    @Test
    fun givenLogoutButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){

    }


    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView(){

    }



    @Test
    fun givenAboutButton_whenClicked_thenShouldGoToAboutView(){

    }

}