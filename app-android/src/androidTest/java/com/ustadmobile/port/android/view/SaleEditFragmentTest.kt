package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.SaleEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView


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
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = Sale().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. saleName = "New Sale"
        }

        init{

        }.run{

            SaleEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val saleList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("Sale data set", "New Sale",
                        saleList.first() .saleName)

            }


        }
    }


    @AdbScreenRecord("given Sale exists when updated then should be updated on database")
    @Test
    fun givenSaleExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingSale = Sale().apply {
            saleName = "New Sale"
            saleUid = dbRule.db.saleDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingSale.saleUid)) {
            SaleEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, Sale::class.java).apply {
            saleName = "Updated Sale"
        }

        init{


        }.run{

            SaleEditScreen {

                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New Sale",
                        defaultGson().fromJson(entityLoadedJson, Sale::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingSale.saleUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated Sale" }
                Assert.assertEquals("Sale name is updated", "Updated Sale",
                        updatedEntityFromDb?.saleName)

            }

        }

    }
}