package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.PersonWithSaleInfoListScreen
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("PersonWithSaleInfo screen tests")
class PersonWithSaleInfoListFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    //TODO: Set up test
//    @AdbScreenRecord("Given list when PersonWithSaleInfo clicked then navigate to PersonWithSaleInfoDetail")
//    @Test
//    fun givenPersonWithSaleInfoListPresent_whenClickOnPersonWithSaleInfo_thenShouldNavigateToPersonWithSaleInfoDetail() {
//
//
//        val testEntity = PersonWithSaleInfo().apply {
//            personWithSaleInfoName = "Test Name"
//            personWithSaleInfoUid = dbRule.db.clazzDao.insert(this)
//        }
//
//        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
//                fragmentArgs = bundleOf()) {
//            PersonWithSaleInfoListFragment().also {
//                it.installNavController(systemImplNavRule.navController)
//            }
//        }
//
//        init{
//
//        }.run{
//
//            PersonWithSaleInfoListScreen{
//
//                recycler{
//
//                    childWith<PersonWithSaleInfoListScreen.PersonWithSaleInfo>{
//                        withDescendant { withTag(testEntity.personWithSaleInfoUid) }
//                    }perform {
//                        title {
//                            click()
//                        }
//                    }
//
//                }
//
//                flakySafely {
//                    Assert.assertEquals("After clicking on item, it navigates to detail view",
//                            R.id.person_with_sale_info_detail_dest, systemImplNavRule.navController.currentDestination?.id)
//                }
//
//
//            }
//
//        }
//    }

}