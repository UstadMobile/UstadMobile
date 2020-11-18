package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.SaleItemEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.SaleItemWithProduct


@AdbScreenRecord("SaleItemEdit screen Test")
class SaleItemEditFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    @AdbScreenRecord("given SaleItem not present when filled then should save to database")
    @Test
    fun givenNoSaleItemPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            SaleItemEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.waitUntilLetOnFragment { it.entity }

        val pinkHat = Product().apply{
            productName = "Pink Hat"
            productActive = true
            productBasePrice = 210F
            productUid = dbRule.db.productDao.insert(this)
        }


        val formVals = SaleItemWithProduct().apply {
            saleItemQuantity = 42
            saleItemPricePerPiece = 420F
            saleItemProductUid = pinkHat.productUid
            saleItemProduct = pinkHat
            deliveredCount = 20
            saleItemDueDate = systemTimeInMillis()
        }

        init{

        }.run{

            SaleItemEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity)

                Thread.sleep(5000)

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val saleItemList = dbRule.db.saleItemDao.findAllActiveLive().waitUntilWithFragmentScenario(
                        fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("SaleItem data set", 42,
                        saleItemList!!.first().saleItemQuantity)
            }
        }
    }


//    @AdbScreenRecord("given SaleItem exists when updated then should be updated on database")
//    @Test
//    fun givenSaleItemExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
//        val existingSaleItem = SaleItem().apply {
//            saleItemName = "New SaleItem"
//            saleItemUid = dbRule.db.saleItemDao.insert(this)
//        }
//
//        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
//                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingSaleItem.saleItemUid)) {
//            SaleItemEditFragment().also {
//                it.installNavController(systemImplNavRule.navController)
//            }
//        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
//                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
//
//        //Freeze and serialize the value as it was first shown to the user
//        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
//        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
//        val newClazzValues = defaultGson().fromJson(entityLoadedJson, SaleItem::class.java).apply {
//            saleItemName = "Updated SaleItem"
//        }
//
//        init{
//
//
//        }.run{
//
//            SaleItemEditScreen {
//
//                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
//                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
//                        testContext = this@run)
//
//                fragmentScenario.clickOptionMenu(R.id.menu_done)
//
//                Assert.assertEquals("Entity in database was loaded for user",
//                        "New SaleItem",
//                        defaultGson().fromJson(entityLoadedJson, SaleItem::class.java).clazzName)
//
//                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingSaleItem.saleItemUid)
//                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated SaleItem" }
//                Assert.assertEquals("SaleItem name is updated", "Updated SaleItem",
//                        updatedEntityFromDb?.saleItemName)
//
//            }
//
//        }
//
//    }
}