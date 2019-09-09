package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.FeedEntry

/**
 * FeedList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface FeedListView : UstadView {

    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the Recycler view.
     *
     * @param feedEntryUmProvider The provider data
     */
    fun setFeedEntryProvider(feedEntryUmProvider: DataSource.Factory<Int, FeedEntry>)

    /**
     * Update number of classes in the summary card in the View.
     *
     * @param num   The number of classes
     */
    fun updateNumClasses(num: Int)

    /**
     * Update the number of students in the summary card in the view.
     *
     * @param num The number of students
     */
    fun updateNumStudents(num: Int)

    /**
     * Update the attendance percentage in the summary card in the view.
     *
     * @param per The percentage number
     */
    fun updateAttendancePercentage(per: Int)

    /**
     * Update the attendance trend metric in the summary card in the view.
     *
     * @param trend The trend based on FeedListView.FEED_LIST_ATTENDANCE_TREND_* flags
     * @param per   The percentage of trend
     */
    fun updateAttendanceTrend(trend: Int, per: Int)

    fun showReportOptionsOnSummaryCard(visible: Boolean)

    fun showSummaryCard(visible: Boolean)

    companion object {

        //View name
        val VIEW_NAME = "FeedList"

        /**
         * Attendance trend flags
         */
        val FEED_LIST_ATTENDANCE_TREND_UP = 1
        val FEED_LIST_ATTENDANCE_TREND_DOWN = 2
        val FEED_LIST_ATTENDANCE_TREND_FLAT = 3
    }

}
