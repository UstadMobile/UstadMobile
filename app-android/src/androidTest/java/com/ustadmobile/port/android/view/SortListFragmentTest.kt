package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.PersonListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Sort list screen tests")
class SortListFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given a person list, when sort option clicked, then show Sort List and change sort")
    @Test
    fun givenPersonPresent_whenOnSortOptionClicked_thenShoulShowBottomFragAndChangeSort() {

        dbRule.insertPersonAndStartSession(Person().apply {
            this.personUid = UmAppDatabaseAndroidClientRule.DEFAULT_ACTIVE_USER_PERSONUID
            this.username = "theanswer"
            this.admin = true
            this.firstNames = "LMNOP"
        })

        val abc = Person().apply {
            this.firstNames = "ABC"
            personUid = dbRule.repo.personDao.insert(this)
        }
        val xyz = Person().apply {
            this.firstNames = "XYZ"
            personUid = dbRule.repo.personDao.insert(this)
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
            val sortOption = it.mUstadListHeaderRecyclerViewAdapter!!.sortOptionSelected
            Assert.assertEquals("order set to default ascending",
                    PersonDao.SORT_FIRST_NAME_ASC, sortOption!!.flag)
        }

        init{

        }.run {

            PersonListScreen{

                recycler{
                    emptyFirstChild{
                        click()
                    }
                }

                sortList{
                    childWith<PersonListScreen.Sort> {
                        withTag(PersonDao.SORT_FIRST_NAME_ASC)
                    }perform {
                        click()
                    }
                }

                fragmentScenario.onFragment {
                    val sortOption = it.mUstadListHeaderRecyclerViewAdapter!!.sortOptionSelected
                    Assert.assertEquals("order changed to descending", PersonDao.SORT_FIRST_NAME_ASC, sortOption!!.flag)
                }
            }
        }
    }


}
