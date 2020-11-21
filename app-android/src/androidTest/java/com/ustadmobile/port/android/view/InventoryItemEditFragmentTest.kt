package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.InventoryItemEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.ext.insertPersonAndGroup

import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*


@AdbScreenRecord("InventoryItemEdit screen Test")
class InventoryItemEditFragmentTest : TestCase(){

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


    @AdbScreenRecord("given InventoryItem not present when filled then should save to database")
    @Test
    fun givenNoInventoryItemPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {


        val pinkHat = Product().apply{
            productName = "Pink Hat"
            productActive = true
            productBasePrice = 210F
            productUid = dbRule.db.productDao.insert(this)

        }

        val weOneGroupPerson = PersonGroup().apply {
            groupName = "Person individual group"
            personGroupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
            groupUid = dbRule.db.personGroupDao.insert(this)
        }

        val weOne = Person().apply {
            firstNames = "We"
            lastName = "One"
            personGoldoziType = Person.GOLDOZI_TYPE_PRODUCER
            personGroupUid = weOneGroupPerson.groupUid
            personUid = dbRule.db.personDao.insert(this)
        }


        val weTwoGroupPerson = PersonGroup().apply {
            groupName = "Person individual group"
            personGroupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
            groupUid = dbRule.db.personGroupDao.insert(this)
        }

        val weTwo = Person().apply {
            firstNames = "We"
            lastName = "Two"
            personGoldoziType = Person.GOLDOZI_TYPE_PRODUCER
            personGroupUid = weTwoGroupPerson.groupUid
            personUid = dbRule.db.personDao.insert(this)
        }


        val weThreeGroupPerson = PersonGroup().apply {
            groupName = "Person individual group"
            personGroupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
            groupUid = dbRule.db.personGroupDao.insert(this)
        }

        val weThree = Person().apply {
            firstNames = "We"
            lastName = "One"
            personGoldoziType = Person.GOLDOZI_TYPE_PRODUCER
            personGroupUid = weThreeGroupPerson.groupUid
            personUid = dbRule.db.personDao.insert(this)
        }


        val leOneWeGroup = PersonGroup().apply {
            groupName = "LeOn'e WE group"
            personGroupFlag = PersonGroup.PERSONGROUP_FLAG_DEFAULT
            groupUid = dbRule.db.personGroupDao.insert(this)
        }

        val weOneAssign = PersonGroupMember(weOne.personUid, leOneWeGroup.groupUid)
        weOneAssign.groupMemberUid = dbRule.db.personGroupMemberDao.insert(weOneAssign)

        val weTwoAssign = PersonGroupMember(weTwo.personUid, leOneWeGroup.groupUid)
        weTwoAssign.groupMemberUid = dbRule.db.personGroupMemberDao.insert(weTwoAssign)

        val weThreeAssign = PersonGroupMember(weThree.personUid, leOneWeGroup.groupUid)
        weThreeAssign.groupMemberUid = dbRule.db.personGroupMemberDao.insert(weThreeAssign)



        val leOnePersonGroup = PersonGroup().apply {
            groupName = "Person individual group"
            personGroupFlag = PersonGroup.PERSONGROUP_FLAG_PERSONGROUP
            groupUid = dbRule.db.personGroupDao.insert(this)
        }

        //Assign to person
        val leOne = Person().apply {
            firstNames = "Le"
            lastName = "One"
            personGroupUid = leOnePersonGroup.groupUid
            personWeGroupUid = leOneWeGroup.groupUid
            personUid = dbRule.db.personDao.insert(this)
        }

        //Assign person to PersonGroup ie: Create PersonGroupMember
        dbRule.db.personGroupMemberDao.insert(
                PersonGroupMember(leOne.personUid, leOne.personGroupUid))




        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            InventoryItemEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }






        val formVals = InventoryItem().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. inventoryItemName = "New InventoryItem"
        }

        init{

        }.run{

            InventoryItemEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val inventoryItemList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("InventoryItem data set", "New InventoryItem",
                        inventoryItemList.first() .inventoryItemName)

            }


        }
    }


    @AdbScreenRecord("given InventoryItem exists when updated then should be updated on database")
    @Test
    fun givenInventoryItemExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingInventoryItem = InventoryItem().apply {
            inventoryItemName = "New InventoryItem"
            inventoryItemUid = dbRule.db.inventoryItemDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingInventoryItem.inventoryItemUid)) {
            InventoryItemEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, InventoryItem::class.java).apply {
            inventoryItemName = "Updated InventoryItem"
        }

        init{


        }.run{

            InventoryItemEditScreen {

                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New InventoryItem",
                        defaultGson().fromJson(entityLoadedJson, InventoryItem::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingInventoryItem.inventoryItemUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated InventoryItem" }
                Assert.assertEquals("InventoryItem name is updated", "Updated InventoryItem",
                        updatedEntityFromDb?.inventoryItemName)

            }

        }

    }
}