/*
package com.ustadmobile.port.android.view

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.port.android.screen.PersonDetailScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("PersonDetail screen Test")
class PersonDetailFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("given person detail when username is null and account management allowed then should hide create account option")
    @Test
    fun givenPersonDetails_whenPersonUsernameIsNullAndCanManageAccount_thenCreateAccountShouldHidden(){

        init{
            launchFragment(withUsername = false, isAdmin = false)
        }.run {

            PersonDetailScreen{

                createAccView{
                    isNotDisplayed()
                }
                changePassView{
                    isNotDisplayed()
                }

            }

        }
    }

    @AdbScreenRecord("given person detail when admin logged and username is null and account management allowed then should show create account option")
    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNullAndCanManageAccount_thenCreateAccountShouldBeShown(){
        init{
            launchFragment(withUsername = false)
        }.run {

            PersonDetailScreen{

                createAccView{
                    isDisplayed()
                }
                changePassView{
                    isNotDisplayed()
                }

            }

        }
    }

    @AdbScreenRecord("given person detail when admin logged in and username is not null and account management allowed then should show change password option")
    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNotNullAndCanManageAccount_thenChangePasswordShouldBeShown(){
        init{
            launchFragment(true)
        }.run {

            PersonDetailScreen{

                createAccView{
                    isNotDisplayed()
                }
                changePassView{
                    isDisplayed()
                }

            }

        }
    }

    @AdbScreenRecord("given person when active user details is opened and account management is allowed then should show change password option")
    @Test
    fun givenPersonDetails_whenOpenedActivePersonDetailPersonAndCanManageAccount_thenChangePasswordShouldBeShown(){
        init{
            launchFragment(false, sameUser = true)
        }.run {

            PersonDetailScreen{

                createAccView{
                    isNotDisplayed()
                }
                changePassView{
                    isDisplayed()
                }

            }

        }
    }



    @AdbScreenRecord("given change password visible when clicked should open person account screen")
    @Test
    fun givenChangePasswordVisible_whenClicked_shouldOpenPersonAccountSection(){

        init{
            launchFragment(true)
        }.run {

            PersonDetailScreen{

                changePassView{
                    isDisplayed()
                    click()
                }
                assertEquals("It navigated to person account screen",
                        R.id.person_account_edit_dest, systemImplNavRule.navController.currentDestination?.id)
            }

        }
    }

    @AdbScreenRecord("given create account visible when clicked should open person account edit screen")
    @Test
    fun givenCreateAccountVisible_whenClicked_shouldOpenPersonAccountEditScreen(){
        init{
            launchFragment(withUsername = false)
        }.run {

            PersonDetailScreen{

                createAccView{
                    isDisplayed()
                    click()
                }
                assertEquals("It navigated to account edit screen",
                R.id.person_account_edit_dest, systemImplNavRule.navController.currentDestination?.id)
            }

        }

    }

    private fun launchFragment(isAdmin: Boolean = true, withUsername: Boolean = true, sameUser: Boolean = false){
        val mPersonUid:Long = if(sameUser) 42 else 43
        val person = PersonWithDisplayDetails().apply {
            firstNames = "Jones"
            lastName = "Doe"
            if(withUsername){
                username = "jones.doe"
            }
            personUid = mPersonUid
            dbRule.db.personDao.insert(this)
        }

        if(!sameUser){
            PersonWithDisplayDetails().apply {
                firstNames = "Admin"
                lastName = "User"
                username = "admin.user"
                personUid = 42
                admin = isAdmin
                dbRule.db.personDao.insert(this)
            }
        }

        val args = mapOf(UstadView.ARG_ENTITY_UID to person.personUid.toString())
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args.toBundle()) {
            PersonDetailFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }
    }

}*/
