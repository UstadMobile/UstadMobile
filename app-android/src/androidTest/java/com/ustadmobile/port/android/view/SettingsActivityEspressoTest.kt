package com.ustadmobile.port.android.view


import android.content.Intent
import android.os.SystemClock
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.staging.port.android.view.SettingsActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsActivityEspressoTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(SettingsActivity::class.java,
            false, false)

    private lateinit var db: UmAppDatabase

    private var context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setup() {

        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        db = UmAppDatabase.getInstance(context)

        insert(db, true)

        //Dont time out for signing in.
        val impl = UstadMobileSystemImpl.instance
        impl.setAppPref(UstadBaseActivity.PREFKEY_LAST_ACTIVE,
                UMCalendarUtil.getDateInMilliPlusDays(0).toString(), context)

    }

    fun launchActivity() {
        val launchIntent = Intent()
        //IF any arguments. Put them here: 
        //launchIntent.putExtra()
        mActivityRule.launchActivity(launchIntent)

        SystemClock.sleep(1000)
    }

    @Test
    fun givenActivityLoads_whenCreated_shouldUpdateView() {
        launchActivity()


        Espresso.onView(withId(R.id.activity_settings_toolbar)).check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withText(R.string.settings)).check(ViewAssertions.matches(withParent(withId(R.id.activity_settings_toolbar))))

        Espresso.onView(withId(R.id.activity_settings_users_cl)).check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.activity_settings_groups_cl)).check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.activity_settings_locations_cl)).check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.activity_settings_locations_imageview)).check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.imageView12)).check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.activity_settings_roles_imageview)).check(ViewAssertions.matches(isDisplayed()))

        Espresso.onView(withId(R.id.activity_settings_roles_title)).check(ViewAssertions.matches(withText(R.string.users)))
        Espresso.onView(withId(R.id.activity_settings_roles_desc)).check(ViewAssertions.matches(withText(R.string.users_settings_desc)))
        Espresso.onView(withId(R.id.textView8)).check(ViewAssertions.matches(withText(R.string.group_setting_desc)))
        Espresso.onView(withId(R.id.textView7)).check(ViewAssertions.matches(withText(R.string.groups)))
        Espresso.onView(withId(R.id.activity_settings_locations_title)).check(ViewAssertions.matches(withText(R.string.locations)))
        Espresso.onView(withId(R.id.activity_settings_locations_desc)).check(ViewAssertions.matches(withText(R.string.locations_setting_desc)))

    }

    @Test
    fun givenActivityLoads_whenUserCreated_shouldGoWithRightArgs(){
        launchActivity()
        Espresso.onView(withId(R.id.activity_settings_users_cl)).perform(click())

        //TODO: Finish this
    }

    @Test
    fun givenActivityLoads_whenGroupCreated_shouldGoWithRightArgs(){
        launchActivity()
        Espresso.onView(withId(R.id.activity_settings_groups_cl)).perform(click())

        //TODO: Finish this
    }

    @Test
    fun givenActivityLoads_whenLocationCreated_shouldGoWithRightArgs(){
        launchActivity()
        Espresso.onView(withId(R.id.activity_settings_locations_cl)).perform(click())

        //TODO: Finish this
    }




    //SETUP//

    var le1Uid: Long = 0
    var le2Uid: Long = 0

    var we1PersonUid: Long = 0L
    var we2PersonUid: Long = 0L

    var sale11Uid: Long = 0L
    var sale22Uid: Long = 0L


    fun insert(db: UmAppDatabase, clear: Boolean = false) {

        if (clear) {
            db.clearAllTables()
        }
        val personDao = db.personDao
        val personGroupDao = db.personGroupDao
        val personGroupMemberDao = db.personGroupMemberDao
        val saleProductDao = db.saleProductDao
        val saleDao = db.saleDao
        val saleItemDao = db.saleItemDao

        //Create two LEs
        val le1 = Person("le1", "Le", "One", true)
        le1Uid = personDao.insert(le1)
        le1.personUid = le1Uid
        val le2 = Person("le2", "Le", "Two", true)
        le2Uid = personDao.insert(le2)
        le2.personUid = le2Uid

        //Create 8 WEs
        val we1 = Person("we1", "We", "One", true, "We1 Summary notes", "123, Fourth Street, Fifth Avenue", "+912121212121")
        val we2 = Person("we2", "We", "Two", true, "We2 Summary notes", "456, Fourth Street, Fifth Avenue", "+821212211221")
        val we3 = Person("we3", "We", "Three", true, "We3 Summary notes", "789, Fourth Street, Fifth Avenue", "00213231232")
        val we4 = Person("we4", "We", "Four", true, "We4 Summary notes", "112, Fourth Street, Fifth Avenue", "57392974323")
        val we5 = Person("we5", "We", "Five", true, "We5  Summary notes", "124, Fourth Street, Fifth Avenue", "1321321321")
        val we6 = Person("we6", "We", "Six", true, "We6  Summary notes", "4242, Fourth Street, Fifth Avenue", "4315747899")
        val we7 = Person("we7", "We", "Seven", true, "We7 Summary notes", "4422, Fourth Street, Fifth Avenue", "123")
        val we8 = Person("we8", "We", "Eight", true, "We8 Summary notes", "42, Fourth Street, Fifth Avenue", "42424224242")

        we1PersonUid = personDao.insert(we1)
        we2PersonUid = personDao.insert(we2)
        val we3PersonUid = personDao.insert(we3)
        val we4PersonUid = personDao.insert(we4)
        val we5PersonUid = personDao.insert(we5)
        val we6PersonUid = personDao.insert(we6)
        val we7PersonUid = personDao.insert(we7)
        val we8PersonUid = personDao.insert(we8)

        //Create LE1's WE Group
        val le1WeGroup = PersonGroup("LE1's WE Group")
        val le1WeGroupUid = personGroupDao.insert(le1WeGroup)
        //Add 1-3 WE's in this group
        val we1GM = PersonGroupMember(we1PersonUid, le1WeGroupUid)
        val we2GM = PersonGroupMember(we2PersonUid, le1WeGroupUid)
        val we3GM = PersonGroupMember(we3PersonUid, le1WeGroupUid)
        personGroupMemberDao.insert(we1GM)
        personGroupMemberDao.insert(we2GM)
        personGroupMemberDao.insert(we3GM)

        //Create LE1's WE Group
        val le2WeGroup = PersonGroup("LE2's WE Group")
        val le2WeGroupUid = personGroupDao.insert(le2WeGroup)
        //Add 1-3 WE's in this group
        val we4GM = PersonGroupMember(we4PersonUid, le2WeGroupUid)
        val we5GM = PersonGroupMember(we5PersonUid, le2WeGroupUid)
        val we6GM = PersonGroupMember(we6PersonUid, le2WeGroupUid)
        personGroupMemberDao.insert(we4GM)
        personGroupMemberDao.insert(we5GM)
        personGroupMemberDao.insert(we6GM)

        //Assign
        le1.mPersonGroupUid = le1WeGroupUid
        le2.mPersonGroupUid = le2WeGroupUid

        //Update
        personDao.update(le1)
        personDao.update(le2)

        //Sale Products
        val saleProduct1 = SaleProduct("Product1", "testing ")
        val saleProduct1Uid = saleProductDao.insert(saleProduct1)

        val saleProduct2 = SaleProduct("Product2", "testing ")
        val saleProduct2Uid = saleProductDao.insert(saleProduct2)

        val saleProduct3 = SaleProduct("Product3", "testing ")
        val saleProduct3Uid = saleProductDao.insert(saleProduct3)

        val saleProduct4 = SaleProduct("Product4", "testing ")
        val saleProduct4Uid = saleProductDao.insert(saleProduct4)


        //Create new Sales for LE1
        //a. Create Sale
        val sale11 = Sale(true)
        sale11.saleTitle = "Test Sale 1"
        sale11.saleDone = true
        sale11.saleNotes = "Test Sale"
        sale11.salePersonUid = le1Uid
        sale11Uid = saleDao.insert(sale11)

        //b. Create SaleItem
        val saleItem11 = SaleItem(saleProduct1Uid, 10, 420L, sale11Uid, 0L)
        saleItem11.saleItemProducerUid = we1PersonUid
        val saleItem12 = SaleItem(saleProduct2Uid, 8, 240, sale11Uid, 0L)
        saleItem12.saleItemProducerUid = we2PersonUid
        saleItemDao.insert(saleItem11)
        saleItemDao.insert(saleItem12)

        //Create new Sales for LE2
        //a. Create Sale
        val sale22 = Sale(true)
        sale22.saleTitle = "Test Sale 2"
        sale22.saleDone = true
        sale22.saleNotes = "Test Sale"
        sale22.salePersonUid = le2Uid
        sale22Uid = saleDao.insert(sale22)
        //b. Create SaleItem
        val saleItem23 = SaleItem(saleProduct3Uid, 10, 420L, sale22Uid, 0L)
        saleItem23.saleItemProducerUid = we4PersonUid
        val saleItem24 = SaleItem(saleProduct4Uid, 8, 240, sale22Uid, 0L)
        saleItem24.saleItemProducerUid = we5PersonUid
        saleItemDao.insert(saleItem23)
        saleItemDao.insert(saleItem24)


    }


}
