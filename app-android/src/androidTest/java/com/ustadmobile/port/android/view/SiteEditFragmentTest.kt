package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.SiteEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.toughra.ustadmobile.R

import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Site

@AdbScreenRecord("WorkSpaceEdit screen Test")
class SiteEditFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    @AdbScreenRecord("given WorkSpace exists when updated then should be updated on database")
    @Test
    fun givenWorkSpaceExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        lateinit var fragmentScenario: FragmentScenario<SiteEditFragment>

        init{
            val existingSite = Site().apply {
                siteName = "New Site"
                siteUid = dbRule.repo.siteDao.insert(this)
            }

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingSite.siteUid)) {
                SiteEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }.withScenarioIdlingResourceRule(crudIdlingResourceRule)

        }.run{
            SiteEditScreen {
                siteTitleInput {
                    edit {
                        withText("New Site")
                        isDisplayed()
                    }
                }

                siteTitleInput {
                    edit {
                        clearText()
                        typeText("Updated Name")
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                flakySafely {
                    val updatedEntity = dbRule.db.siteDao.getSite()
                    Assert.assertEquals("Site name is updated", "Updated Name",
                            updatedEntity?.siteName)
                }
            }
        }

    }
}