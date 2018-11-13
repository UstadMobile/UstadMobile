package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.FeedEntry;

/**
 * FeedList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface FeedListView extends UstadView {

    String VIEW_NAME = "FeedList";

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

    void updateNumClasses(int num);

    void updateNumStudents(int num);

    void updateAttendancePercentage(int per);

    void updateAttendanceTrend(int trend, int per);

}
