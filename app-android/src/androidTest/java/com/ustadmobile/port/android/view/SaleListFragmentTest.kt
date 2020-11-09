package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.SaleListScreen
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Sale screen tests")
class SaleListFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when Sale clicked then navigate to SaleDetail")
    @Test
    fun givenSaleListPresent_whenClickOnSale_thenShouldNavigateToSaleDetail() {
        val person = Person().apply{
            firstNames = "Admin"
            lastName = "One"
            admin = true
            personUid = dbRule.db.personDao.insert(this)
        }
        val producer = Person().apply{
            firstNames = "We"
            lastName = "One"
            personUid = dbRule.db.personDao.insert(this)
        }
        dbRule.account.personUid = person.personUid


        val pinkHat = Product().apply {
            productName = "Pink Hat"
            productActive = true
            productBasePrice = 12.24f
            productUid = dbRule.db.productDao.insert(this)
        }

        val testEntity = Sale().apply {
            saleTitle = "Test sale title"
            saleActive = true
            saleCreationDate = systemTimeInMillis()
            saleDueDate = UMCalendarUtil.getDateInMilliPlusDays(2)
            saleLastUpdateDate = systemTimeInMillis()
            salePersonUid = person.personUid
            saleNotes = "Blah blah"
            saleDone = true
            saleDiscount = 100

            saleUid = dbRule.db.saleDao.insert(this)

            SaleItem().apply {
                saleItemActive = true
                saleItemProductUid = pinkHat.productUid
                saleItemProducerUid = producer.personUid
                saleItemSaleUid = saleUid
                saleItemQuantity = 42
                saleItemPricePerPiece = 42.0F
                saleItemCurrency = "Afs"
                saleItemSold = true
                saleItemDiscount = 4.2F
                saleItemCreationDate = systemTimeInMillis()
                saleItemUid = dbRule.db.saleItemDao.insert(this)


            }
        }

        val testEntity2 = Sale().apply {
            saleTitle = "Test sale title 2"
            saleActive = true
            saleCreationDate = systemTimeInMillis()
            saleDueDate = UMCalendarUtil.getDateInMilliPlusDays(2)
            saleLastUpdateDate = systemTimeInMillis()
            salePersonUid = person.personUid
            saleNotes = "Blah blah"
            saleDone = true
            saleDiscount = 100

            saleUid = dbRule.db.saleDao.insert(this)
            SaleItem().apply {
                saleItemActive = true
                saleItemProductUid = pinkHat.productUid
                saleItemProducerUid = producer.personUid
                saleItemSaleUid = saleUid
                saleItemQuantity = 22
                saleItemPricePerPiece = 22.0F
                saleItemCurrency = "Afs"
                saleItemSold = true
                saleItemDiscount = 4.2F
                saleItemCreationDate = systemTimeInMillis()
                saleItemUid = dbRule.db.saleItemDao.insert(this)
            }
        }

        val testEntity3 = Sale().apply {
            saleTitle = "Test sale title 3"
            saleActive = true
            saleCreationDate = systemTimeInMillis()
            saleDueDate = UMCalendarUtil.getDateInMilliPlusDays(2)
            saleLastUpdateDate = systemTimeInMillis()
            salePersonUid = person.personUid
            saleNotes = "Blah blah"
            saleDone = true
            saleDiscount = 100

            saleUid = dbRule.db.saleDao.insert(this)
            SaleItem().apply {
                saleItemActive = true
                saleItemProductUid = pinkHat.productUid
                saleItemProducerUid = producer.personUid
                saleItemSaleUid = saleUid
                saleItemQuantity = 12
                saleItemPricePerPiece = 12.0F
                saleItemCurrency = "Afs"
                saleItemSold = true
                saleItemDiscount = 4.2F
                saleItemCreationDate = systemTimeInMillis()
                saleItemUid = dbRule.db.saleItemDao.insert(this)
            }
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            SaleListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
        }

        init{

        }.run{

            SaleListScreen{

                recycler{

                    childWith<SaleListScreen.Sale>{
                        withDescendant { withTag(testEntity.saleUid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    //TODO
//                    Assert.assertEquals("After clicking on item, it navigates to detail view",
//                            R.id.sale_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}