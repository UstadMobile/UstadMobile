package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.lib.db.entities.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.Espresso.onData

import androidx.test.espresso.action.ViewActions.click
import org.hamcrest.CoreMatchers.*


class PersonWithSalesInfoListActivityEspressoTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(PersonWithSaleInfoListActivity::class.java,
            false, false)

    private lateinit var db: UmAppDatabase

    private var context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setup(){
        db = UmAppDatabase.getInstance(context)

        insert(db, true)

        //Dont time out for signing in.
        val impl = UstadMobileSystemImpl.instance
        impl.setAppPref(UstadBaseActivity.PREFKEY_LAST_ACTIVE,
                UMCalendarUtil.getDateInMilliPlusDays(0).toString(), context)

    }

    fun launchActivity(leUid:Long = le1Uid){
        val launchIntent = Intent()
        launchIntent.putExtra(PersonWithSaleInfoListView.Companion.ARG_LE_UID, leUid.toString())
        mActivityRule.launchActivity(launchIntent)
        SystemClock.sleep(1000)
    }

    @Test
    fun givenActivityLoadsWithArg_whenCreated_shouldDisplayRecyclerView(){
        launchActivity()

        val textView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_name), ViewMatchers.withText("We One"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                0)),
                                0),
                        ViewMatchers.isDisplayed()))
        textView.check(ViewAssertions.matches(ViewMatchers.withText("We One")))

        val textView2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_name), ViewMatchers.withText("We Two"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                1)),
                                0),
                        ViewMatchers.isDisplayed()))
        textView2.check(ViewAssertions.matches(ViewMatchers.withText("We Two")))

        val textView3 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_name), ViewMatchers.withText("We Three"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                2)),
                                0),
                        ViewMatchers.isDisplayed()))
        textView3.check(ViewAssertions.matches(ViewMatchers.withText("We Three")))

        val textView4 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_sale_amount), ViewMatchers.withText("4200"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                0)),
                                1),
                        ViewMatchers.isDisplayed()))
        textView4.check(ViewAssertions.matches(ViewMatchers.withText("4200")))

        val textView5 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_sale_amount), ViewMatchers.withText("1920"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                1)),
                                1),
                        ViewMatchers.isDisplayed()))
        textView5.check(ViewAssertions.matches(ViewMatchers.withText("1920")))

        val textView6 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_sale_amount), ViewMatchers.withText("0"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                2)),
                                1),
                        ViewMatchers.isDisplayed()))
        textView6.check(ViewAssertions.matches(ViewMatchers.withText("0")))

    }

    @Test
    fun givenActivityLoads_whenCreated_shouldDisplayTitle(){
        launchActivity()
        onView(withId(R.id.activity_person_with_saleinfo_list_toolbar)).check(matches(isDisplayed()))
        onView(withText(R.string.my_women_entrepreneurs)).check(matches(withParent(withId(R.id.activity_person_with_saleinfo_list_toolbar))))
    }

    @Test
    fun givenActivityLoas_whenCreated_shouldDisplaySearchMenuItem(){
        launchActivity()

        val appCompatImageView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.search_button), ViewMatchers.withContentDescription("Search"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.search_bar),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.action_search),
                                                0)),
                                1),
                        ViewMatchers.isDisplayed()))
        appCompatImageView.perform(ViewActions.click())
        SystemClock.sleep(1000)

        val appCompatImageView2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.search_close_btn), ViewMatchers.withContentDescription("Clear query"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.search_plate),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.search_edit_frame),
                                                1)),
                                1),
                        ViewMatchers.isDisplayed()))
        appCompatImageView2.perform(ViewActions.click())

    }

    @Test
    fun givenActivityoads_whenCreated_shouldDisplaySpinner(){
        launchActivity()

        val appCompatSpinner = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_spinner),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(android.R.id.content),
                                        0),
                                1),
                        ViewMatchers.isDisplayed()))
        appCompatSpinner.perform(ViewActions.click())

        val checkedTextView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(android.R.id.text1),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(FrameLayout::class.java),
                                        0),
                                0),
                        ViewMatchers.isDisplayed()))
        checkedTextView.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        val checkedTextView2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(android.R.id.text1),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(FrameLayout::class.java),
                                        0),
                                1),
                        ViewMatchers.isDisplayed()))
        checkedTextView2.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        val checkedTextView3 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(android.R.id.text1),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(FrameLayout::class.java),
                                        0),
                                2),
                        ViewMatchers.isDisplayed()))
        checkedTextView3.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        val checkedTextView4 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(android.R.id.text1),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(FrameLayout::class.java),
                                        0),
                                0),
                        ViewMatchers.isDisplayed()))
        checkedTextView4.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun givenActivityLoads_whenSortedByAmount_shouldUpdateRV(){
        launchActivity()

        onView(withId(R.id.activity_person_with_saleinfo_list_spinner)).perform(click())
        onData(anything()).atPosition(2).perform(click())
        SystemClock.sleep(1000)

        //TODO: Check if its in order


    }

    @Test
    fun givenActivityLoadsWithInvalidArgs_whenCreated_shouldDisplayEmptyRV(){
        launchActivity(42L)
    }

    @Test
    fun givenActivityCreated_whenWEClicked_shouldGoToDetailPage(){
        launchActivity()

    }

    @Test
    fun givenActivityCreated_whenSearched_shouldGiveCorrectResult(){
        launchActivity()

        val appCompatImageView3 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.search_button), ViewMatchers.withContentDescription("Search"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.search_bar),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.action_search),
                                                0)),
                                1),
                        ViewMatchers.isDisplayed()))
        appCompatImageView3.perform(ViewActions.click())

        val searchAutoComplete = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.search_src_text),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.search_plate),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        ViewMatchers.isDisplayed()))
        searchAutoComplete.perform(ViewActions.replaceText("two"), ViewActions.closeSoftKeyboard())

        val textView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_name), ViewMatchers.withText("We Two"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                0)),
                                0),
                        ViewMatchers.isDisplayed()))
        textView.check(ViewAssertions.matches(ViewMatchers.withText("We Two")))

        val textView2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_sale_amount), ViewMatchers.withText("1920"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                0)),
                                1),
                        ViewMatchers.isDisplayed()))
        textView2.check(ViewAssertions.matches(ViewMatchers.withText("1920")))

        val textView3 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.item_person_sale_amount), ViewMatchers.withText("1920"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.item_person_cl),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.activity_person_with_saleinfo_list_recyclerview),
                                                0)),
                                1),
                        ViewMatchers.isDisplayed()))
        textView3.check(ViewAssertions.matches(ViewMatchers.withText("1920")))
    }


    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }



    var le1Uid:Long = 0
    var le2Uid:Long = 0


    fun insert(db: UmAppDatabase, clear:Boolean = false){

        if(clear){
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
        val we1 = Person("we1", "We", "One", true, "We1 Summary notes", "123, Fourth Street, Fifth Avenue")
        val we2 = Person("we2", "We", "Two", true, "We2 Summary notes", "456, Fourth Street, Fifth Avenue")
        val we3 = Person("we3", "We", "Three", true, "We3 Summary notes", "789, Fourth Street, Fifth Avenue")
        val we4 = Person("we4", "We", "Four", true, "We4 Summary notes", "112, Fourth Street, Fifth Avenue")
        val we5 = Person("we5", "We", "Five", true, "We5  Summary notes", "124, Fourth Street, Fifth Avenue")
        val we6 = Person("we6", "We", "Six", true, "We6  Summary notes", "4242, Fourth Street, Fifth Avenue")
        val we7 = Person("we7", "We", "Seven", true, "We7 Summary notes", "4422, Fourth Street, Fifth Avenue")
        val we8 = Person("we8", "We", "Eight", true, "We8 Summary notes", "42, Fourth Street, Fifth Avenue")

        val we1PersonUid = personDao.insert(we1)
        val we2PersonUid = personDao.insert(we2)
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
        val sale11Uid = saleDao.insert(sale11)
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
        val sale22Uid = saleDao.insert(sale22)
        //b. Create SaleItem
        val saleItem23 = SaleItem(saleProduct3Uid, 10, 420L, sale22Uid, 0L)
        saleItem23.saleItemProducerUid = we4PersonUid
        val saleItem24 = SaleItem(saleProduct4Uid, 8, 240, sale22Uid, 0L)
        saleItem24.saleItemProducerUid = we5PersonUid
        saleItemDao.insert(saleItem23)
        saleItemDao.insert(saleItem24)


    }


}