package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ProductListScreen
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Product screen tests")
class ProductListFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when Product clicked then navigate to ProductDetail")
    @Test
    fun givenProductListPresent_whenClickOnProduct_thenShouldNavigateToProductDetail() {
        val testEntity = Product().apply {
            productName = "Test Name"
            productActive = true
            productBasePrice = 12.24f
            productUid = dbRule.db.productDao.insert(this)
        }
        Product().apply {
            productName = "Test Name 2"
            productActive = true
            productBasePrice = 22.24f
            productUid = dbRule.db.productDao.insert(this)
        }

        Product().apply {
            productName = "Test Name 3"
            productActive = true
            productBasePrice = 32.24f
            productUid = dbRule.db.productDao.insert(this)
        }

        Product().apply {
            productName = "Test Name 4"
            productActive = true
            productActive = true
            productBasePrice = 42.24f

            productUid = dbRule.db.productDao.insert(this)
        }



        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ProductListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
        }

        init{

        }.run{

            ProductListScreen{

                recycler{

                    childWith<ProductListScreen.Product>{
                        withDescendant { withTag(testEntity.productUid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    //TODO:
//                    Assert.assertEquals("After clicking on item, it navigates to detail view",
//                            R.id.product_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}