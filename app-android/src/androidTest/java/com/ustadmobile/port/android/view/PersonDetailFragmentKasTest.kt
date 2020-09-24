package com.ustadmobile.port.android.view

import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.port.android.screen.PersonDetailScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("KAS PersonDetail screen Test")
class PersonDetailFragmentKasTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given person when active user details is opened and account management is allowed then should show change password option")
    @Test
    fun givenPersonDetails_whenOpenedActivePersonDetailPersonAndCanManageAccount_thenChangePasswordShouldBeShown(){
        before {
            launchFragment(false, sameUser = true)
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
            launchFragment(withUsername = false)
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


}