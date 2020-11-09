package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.hours
import com.ustadmobile.port.android.screen.ProductEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Schedule


@AdbScreenRecord("ProductEdit screen Test")
class ProductEditFragmentTest : TestCase(){

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


    @AdbScreenRecord("given Product not present when filled then should save to database")
    @Test
    fun givenNoProductPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            ProductEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf()
            }
        }

        val currentEntity = fragmentScenario.waitUntilLetOnFragment { it.entity }
        val formVals = Product().apply {
            //e.g. productName = "New Product"
            productName = "New Product"
            productActive = true
        }

        val categories = listOf(Category().apply {
            categoryActive = true
            categoryName = "Category A"
        })

        init{

        }.run{

            ProductEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity, categories)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Thread.sleep(2000)

                val productList = dbRule.db.productDao.findAllActiveRolesLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("Product data set", "New Product",
                        productList!!.first().productName)

            }


        }
    }

//
//    @AdbScreenRecord("given Product exists when updated then should be updated on database")
//    @Test
//    fun givenProductExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
//        val existingProduct = Product().apply {
//            productName = "New Product"
//            productUid = dbRule.db.productDao.insert(this)
//        }
//
//        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
//                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingProduct.productUid)) {
//            ProductEditFragment().also {
//                it.installNavController(systemImplNavRule.navController)
//            }
//        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
//                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
//
//        //Freeze and serialize the value as it was first shown to the user
//        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
//        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
//        val newClazzValues = defaultGson().fromJson(entityLoadedJson, Product::class.java).apply {
//            productName = "Updated Product"
//        }
//
//        init{
//
//
//        }.run{
//
//            ProductEditScreen {
//
//                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
//                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
//                        testContext = this@run)
//
//                fragmentScenario.clickOptionMenu(R.id.menu_done)
//
//                Assert.assertEquals("Entity in database was loaded for user",
//                        "New Product",
//                        defaultGson().fromJson(entityLoadedJson, Product::class.java).clazzName)
//
//                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingProduct.productUid)
//                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated Product" }
//                Assert.assertEquals("Product name is updated", "Updated Product",
//                        updatedEntityFromDb?.productName)
//
//            }
//
//        }
//
//    }
}