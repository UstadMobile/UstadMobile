package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.SaleEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.UMCalendarUtil

import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*


@AdbScreenRecord("SaleEdit screen Test")
class SaleEditFragmentTest : TestCase(){

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


    @AdbScreenRecord("given Sale not present when filled then should save to database")
    @Test
    fun givenNoSalePresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            SaleEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf()
            }
        }

        val customerA = Person().apply {
            firstNames = "Person"
            lastName = "A"
            personUid = dbRule.db.personDao.insert(this)
        }

        val provinceDubai = Location().apply{
            locationTitle = "Dubai"
            locationActive = true
            locationUid = dbRule.db.locationDao.insert(this)
        }

        val currentEntity = fragmentScenario.waitUntilLetOnFragment { it.entity }
        val formVals = Sale().apply {
            saleNotes = "This is a note"
            saleCustomerUid = customerA.personUid
            saleLocationUid = provinceDubai.locationUid
            saleActive = true
            saleDueDate = UMCalendarUtil.getDateInMilliPlusDays(3)
            saleTitle = "This should not be seen"
        }
        val pinkHat = Product().apply {
            productName = "Pink Hat"
            productActive = true
            productUid = dbRule.db.productDao.insert(this)
        }
        val yellowShorts = Product().apply {
            productName = "Yello Shorts"
            productActive = true
            productUid = dbRule.db.productDao.insert(this)
        }
        val saleItems = listOf(
                SaleItemWithProduct().apply{
                    saleItemProduct = pinkHat
                    saleItemProductUid = pinkHat.productUid
                    saleItemPricePerPiece = 420F
                    saleItemQuantity = 42
                    saleItemCreationDate = systemTimeInMillis()

                    saleItemUid = dbRule.db.saleItemDao.insert(this)
                },
                SaleItemWithProduct().apply {
                    saleItemProduct = yellowShorts
                    saleItemProductUid = yellowShorts.productUid
                    saleItemPricePerPiece = 210F
                    saleItemQuantity = 21
                    saleItemCreationDate = systemTimeInMillis()

                    saleItemUid = dbRule.db.saleItemDao.insert(this)
                }
        )

        val saleDeliveries = listOf(
                SaleDelivery().apply {
                    saleDeliveryActive = true
                    saleDeliveryDate = systemTimeInMillis()
                    saleDeliveryUid = dbRule.db.saleDeliveryDao.insert(this)
                })

        val salePayments = listOf(SalePayment().apply {
            salePaymentActive = true
            salePaymentCurrency = "Afs"
            salePaymentPaidAmount = 420
            salePaymentPaidDate = systemTimeInMillis()
            salePaymentDone = true
            salePaymentUid = dbRule.db.salePaymentDao.insert(this)
        })

        init{

        }.run{

            SaleEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity,
                        saleItems, listOf(), saleDeliveries, listOf(), salePayments,
                        listOf())

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                Thread.sleep(2000)

                val saleList = dbRule.db.saleDao.findAllSalesList()

                Thread.sleep(2000)

                Assert.assertEquals("Product data set", "This is a note",
                        saleList!!.first().saleNotes)

            }


        }
    }


//    @AdbScreenRecord("given Sale exists when updated then should be updated on database")
//    @Test
//    fun givenSaleExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
//        val existingSale = Sale().apply {
//            saleName = "New Sale"
//            saleUid = dbRule.db.saleDao.insert(this)
//        }
//
//        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
//                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingSale.saleUid)) {
//            SaleEditFragment().also {
//                it.installNavController(systemImplNavRule.navController)
//            }
//        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
//                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
//
//        //Freeze and serialize the value as it was first shown to the user
//        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
//        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
//        val newClazzValues = defaultGson().fromJson(entityLoadedJson, Sale::class.java).apply {
//            saleName = "Updated Sale"
//        }
//
//        init{
//
//
//        }.run{
//
//            SaleEditScreen {
//
//                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
//                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
//                        testContext = this@run)
//
//                fragmentScenario.clickOptionMenu(R.id.menu_done)
//
//                Thread.sleep(2000)
//
//                val saleList = dbRule.db.saleDao.findAllSalesList()
//
//                Thread.sleep(2000)
//
//                Assert.assertEquals("Product data set", "This is a note",
//                        saleList!!.first().saleNotes)
//
////                Assert.assertEquals("Entity in database was loaded for user",
////                        "New Sale",
////                        defaultGson().fromJson(entityLoadedJson, Sale::class.java).clazzName)
////
////                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingSale.saleUid)
////                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated Sale" }
////                Assert.assertEquals("Sale name is updated", "Updated Sale",
////                        updatedEntityFromDb?.saleName)
//
//            }
//
//        }
//
//    }
}