package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.ClazzListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Class list screen tests")
class ClazzListFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("List screen should show class in database and allow clicking on item")
    @Test
    fun givenClazzPresent_whenClickOnClazz_thenShouldNavigateToClazzDetail() {
        val testEntity = Clazz().apply {
            clazzName = "Test Name"
            isClazzActive = true
            clazzUid = dbRule.repo.clazzDao.insert(this)
        }



        init {

            dbRule.insertPersonAndStartSession(Person().apply {
                admin = true
                firstNames = "Test"
                lastName = "User"
            })

            val fragmentScenario = launchFragmentInContainer(
                    bundleOf(), themeResId = R.style.UmTheme_App){
                ClazzListFragment().also {
                    it.installNavController(systemImplNavRule.navController,
                            initialDestId = R.id.clazz_list_dest)
                } }

            fragmentScenario.onFragment {
                Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
            }

        }.run {

            ClazzListScreen{

                recycler{

                    childWith<ClazzListScreen.MainItem> {
                        withTag(testEntity.clazzUid)
                    } perform {
                        click()
                    }

                }

            }

            flakySafely {
                Assert.assertEquals("After clicking on item, it navigates to detail view",
                        R.id.clazz_detail_dest, systemImplNavRule.navController.currentDestination?.id)
            }
            val currentArgs = systemImplNavRule.navController.currentDestination?.arguments
            //Note: as of 02/June/2020 arguments were missing even though they were given

        }



    }

}