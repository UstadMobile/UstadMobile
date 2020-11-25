package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.SaleDeliveryEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView


@AdbScreenRecord("SaleDeliveryEdit screen Test")
class SaleDeliveryEditFragmentTest : TestCase(){

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


    @AdbScreenRecord("given SaleDelivery not present when filled then should save to database")
    @Test
    fun givenNoSaleDeliveryPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            SaleDeliveryEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = SaleDelivery().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. saleDeliveryName = "New SaleDelivery"
        }

        init{

        }.run{

            SaleDeliveryEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val saleDeliveryList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("SaleDelivery data set", "New SaleDelivery",
                        saleDeliveryList.first() .saleDeliveryName)

            }


        }
    }


    @AdbScreenRecord("given SaleDelivery exists when updated then should be updated on database")
    @Test
    fun givenSaleDeliveryExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingSaleDelivery = SaleDelivery().apply {
            saleDeliveryName = "New SaleDelivery"
            saleDeliveryUid = dbRule.db.saleDeliveryDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingSaleDelivery.saleDeliveryUid)) {
            SaleDeliveryEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, SaleDelivery::class.java).apply {
            saleDeliveryName = "Updated SaleDelivery"
        }

        init{


        }.run{

            SaleDeliveryEditScreen {

                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New SaleDelivery",
                        defaultGson().fromJson(entityLoadedJson, SaleDelivery::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingSaleDelivery.saleDeliveryUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated SaleDelivery" }
                Assert.assertEquals("SaleDelivery name is updated", "Updated SaleDelivery",
                        updatedEntityFromDb?.saleDeliveryName)

            }

        }

    }
}