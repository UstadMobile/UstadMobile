package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.nhaarman.mockitokotlin2.description
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.port.android.screen.ProductDetailScreen
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord(" ProductDetail screen Test")
class ProductDetailFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given Product exists when launched then show Product")
    @Test
    fun givenProductExists_whenLaunched_thenShouldShowProduct() {
        val testEntity = Product().apply {
            productName = "Test Name"
            productActive = true
            productDesc = "Blah Blah Blah"
            productBasePrice = 12.24f
            productUid = dbRule.db.productDao.insert(this)
        }
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(ARG_ENTITY_UID to testEntity.productUid)) {
            ProductDetailFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ProductDetailScreen{

                description{
                    isDisplayed()
                    hasText(testEntity.productDesc!!)
                }
                price{
                    isDisplayed()
                    hasText(testEntity.productBasePrice.toString())
                }

                //TODO:

                //1. Test Picture
                //2. Test categories list
                //3. Test click on receive delivery
                //4. Test click on record Sale
                //5. Test Stock list
                //6. Test history list
                //7. Test click edit.

            }


        }

    }

}