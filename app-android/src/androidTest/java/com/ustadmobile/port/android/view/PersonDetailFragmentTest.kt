package com.ustadmobile.port.android.view

import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.PersonDetailScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("PersonDetail screen Test")
class PersonDetailFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()



    private fun launchFragment(
        activeUserIsAdmin: Boolean = true,
        personHasUsername: Boolean = true,
        activeUserIsPersonDisplayed: Boolean = false
    ){

        dbRule.insertPersonAndStartSession(Person().apply {
            personUid = UmAppDatabaseAndroidClientRule.DEFAULT_ACTIVE_USER_PERSONUID
            firstNames = "Jones"
            lastName = "Doe"
            username = "jones.doe"
            admin = activeUserIsAdmin
        })


        if(!activeUserIsPersonDisplayed) {
            //Create an extra person
            runBlocking {
                dbRule.repo.insertPersonAndGroup(Person().apply {
                    personUid = 43L
                    firstNames = "Jones"
                    lastName = "Doe"
                    if(personHasUsername){
                        username = "jones.doe"
                    }
                })
            }
        }

        val displayPersonUid = if(activeUserIsPersonDisplayed) {
            UmAppDatabaseAndroidClientRule.DEFAULT_ACTIVE_USER_PERSONUID
        }else {
            43L
        }



        val args = mapOf(UstadView.ARG_ENTITY_UID to displayPersonUid.toString())
        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
            fragmentArgs = args.toBundle()) {
            PersonDetailFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }
    }

    @AdbScreenRecord("given person detail when username is null and account management allowed then should hide create account option")
    @Test
    fun givenPersonDetails_whenPersonUsernameIsNullAndCantManageAccount_thenCreateAccountShouldHidden(){

        init{
            launchFragment(personHasUsername = false, activeUserIsAdmin = false,
                activeUserIsPersonDisplayed = false)
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


    @AdbScreenRecord("given person when active user details is opened and account management is allowed then should show change password option")
    @Test
    fun givenPersonDetails_whenOpenedActivePersonDetailPersonAndCanManageAccount_thenChangePasswordShouldBeShown(){
        before {
            launchFragment(false, activeUserIsPersonDisplayed = true)
        }.after {

        }.run {

            PersonDetailScreen {
                changePassView{
                    isDisplayed()
                }
                createAccView{
                    isNotDisplayed()
                }
            }
        }
    }



    @AdbScreenRecord("given person detail when admin logged and username is null and account management allowed then should show create account option")
    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNullAndCanManageAccount_thenCreateAccountShouldBeShown(){
        before {
            launchFragment(personHasUsername = false, activeUserIsAdmin = true)
        }.after {

        }.run {

            PersonDetailScreen {
                changePassView{
                   isNotDisplayed()
                }
                createAccView{
                    isDisplayed()
                }
            }
        }
    }

    @AdbScreenRecord("given person detail when admin logged in and username is not null and account management allowed then should show change password option")
    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNotNullAndCanManageAccount_thenChangePasswordShouldBeShown(){
        before {
            launchFragment(true)
        }.after {

        }.run {

            PersonDetailScreen {
                changePassView{
                    isDisplayed()
                }
                createAccView{
                    isNotDisplayed()
                }
            }
        }
    }

    @AdbScreenRecord("given create account visible when clicked should open person account edit screen")
    @Test
    fun givenCreateAccountVisible_whenClicked_shouldOpenPersonAccountEditScreen(){


        before {
            launchFragment(personHasUsername = false)
        }.after {

        }.run {

            PersonDetailScreen {
                createAccView{
                    isDisplayed()
                    click()
                }
            }
            flakySafely {
                assertEquals("It navigated to account edit screen",
                        R.id.person_account_edit_dest, systemImplNavRule.navController.currentDestination?.id)
            }
        }

    }

    @AdbScreenRecord("given change password visible when clicked should open person account screen")
    @Test
    fun givenChangePasswordVisible_whenClicked_shouldOpenPersonAccountSection(){

        before {
            launchFragment(true)
        }.after {

        }.run {

            PersonDetailScreen {
                changePassView{
                    isDisplayed()
                    click()
                }
            }
            flakySafely {
                assertEquals("It navigated to person account screen",
                        R.id.person_account_edit_dest, systemImplNavRule.navController.currentDestination?.id)
            }
        }
    }





}
