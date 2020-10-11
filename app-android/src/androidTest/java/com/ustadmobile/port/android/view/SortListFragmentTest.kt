package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Sort list screen tests")
class SortListFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @AdbScreenRecord("given a person list, when sort option clicked, then show Sort List and change sort")
    //@Test
    fun givenPersonPresent_whenOnSortOptionClicked_thenShoulShowBottomFragAndChangeSort() {

        val admin = Person().apply {
            this.personUid = 42
            this.username = "theanswer"
            this.admin = true
            this.firstNames = "LMNOP"
            dbRule.db.personDao.insert(this)
        }

        val abc = Person().apply {
            this.firstNames = "ABC"
            personUid = dbRule.db.personDao.insert(this)
        }
        val xyz = Person().apply {
            this.firstNames = "XYZ"
            personUid = dbRule.db.personDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(
                bundleOf(), themeResId = R.style.UmTheme_App) {
            PersonListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
        }

        fragmentScenario.onFragment {
            val sortOption = it.mNewItemRecyclerViewAdapter!!.sortOptionSelected
            Assert.assertEquals("order set to default ascending",
                    PersonDao.SORT_FIRST_NAME_ASC, sortOption!!.flag)
        }

        onView(withId(R.id.item_sort_selected_layout)).perform(click())

        onView(withId(R.id.fragment_sort_order_list)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(withTagValue(Matchers.equalTo(PersonDao.SORT_FIRST_NAME_ASC)),
                        click()))

        fragmentScenario.onFragment {
            val sortOption = it.mNewItemRecyclerViewAdapter!!.sortOptionSelected
            Assert.assertEquals("order changed to descending", PersonDao.SORT_FIRST_NAME_ASC, sortOption!!.flag)
        }

    }


}
