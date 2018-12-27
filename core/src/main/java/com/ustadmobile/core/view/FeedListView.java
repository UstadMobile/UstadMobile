package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.FeedEntry;

/**
 * FeedList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface FeedListView extends UstadView {

    //View name
    String VIEW_NAME = "FeedList";

    /**
     * Attendance trend flags
     */
    int FEED_LIST_ATTENDANCE_TREND_UP = 1;
    int FEED_LIST_ATTENDANCE_TREND_DOWN = 2;
    int FEED_LIST_ATTENDANCE_TREND_FLAT = 3;

    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the Recycler view.
     *
     * @param feedEntryUmProvider The provider data
     */
    void setFeedEntryProvider(UmProvider<FeedEntry> feedEntryUmProvider);

    /**
     * Update number of classes in the summary card in the View.
     *
     * @param num   The number of classes
     */
    void updateNumClasses(int num);

    /**
     * Update the number of students in the summary card in the view.
     *
     * @param num The number of students
     */
    void updateNumStudents(int num);

    /**
     * Update the attendance percentage in the summary card in the view.
     *
     * @param per The percentage number
     */
    void updateAttendancePercentage(int per);

    /**
     * Update the attendance trend metric in the summary card in the view.
     *
     * @param trend The trend based on FeedListView.FEED_LIST_ATTENDANCE_TREND_* flags
     * @param per   The percentage of trend
     */
    void updateAttendanceTrend(int trend, int per);

    void showReportOptionsOnSummaryCard(boolean visible);

    void showSummaryCard(boolean visible);

}
