package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.@BaseFileName@Screen
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("@Entity@ screen tests")
class @BaseFileName@FragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when @Entity@ clicked then navigate to @Entity@Detail")
    @Test
    fun given@Entity@ListPresent_whenClickOn@Entity@_thenShouldNavigateTo@Entity@Detail() {
        val testEntity = @Entity@().apply {
            @Entity_VariableName@Name = "Test Name"
            @Entity_VariableName@Uid = dbRule.db.clazzDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            @BaseFileName@Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            @BaseFileName@Screen{

                recycler{

                    childWith<@BaseFileName@Screen.@Entity@>{
                        withDescendant { withTag(testEntity.@Entity_VariableName@Uid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.@Entity_SnakeCase@_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}