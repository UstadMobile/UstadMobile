package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Report list screen test")
class ReportListFragmentTest{

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @Before
    fun setup(){
        runBlocking {
            dbRule.db.insertTestStatements()
        }
    }

    @AdbScreenRecord("given report in list, when clicked, go to detail report")
    @Test
    fun givenReportPresent_whenClickOnReport_thenShouldNavigateToReportDetail() {
        val testEntity = Report().apply {
            reportTitle = "Test Name"
            chartType = Report.BAR_CHART
            yAxis = Report.AVG_DURATION
            xAxis = Report.MONTH
            fromDate =  DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportUid = dbRule.db.reportDao.insert(this)
        }

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ReportListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        //Note: In order for clicking on the RecyclerView item to work, you MUST set to the tag of
        // the viewholder in the onBind method of the RecyclerView.Adapter. This must be set in the
        // method itself, not via data binding.
        onView(withId(R.id.fragment_list_recyclerview)).perform(
                actionOnItem<RecyclerView.ViewHolder>(withTagValue(equalTo(testEntity.reportUid)),
                        click()))

        Assert.assertEquals("After clicking on item, it navigates to detail view",
                R.id.report_detail_dest, systemImplNavRule.navController.currentDestination?.id)
        val currentArgs = systemImplNavRule.navController.currentDestination?.arguments
        //Note: as of 02/June/2020 arguments were missing even though they were given
    }

}