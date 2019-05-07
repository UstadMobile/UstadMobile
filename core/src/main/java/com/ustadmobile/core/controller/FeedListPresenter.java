package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.FeedListView;
import com.ustadmobile.core.view.ReportSelectionView;
import com.ustadmobile.lib.db.entities.ClazzAverage;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Role;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ReportEditView.ARG_REPORT_NAME;

/**
 * The FeedList's Presenter - responsible for the logic to display all feeds and action on opening
 * them to the right place.
 * This presenter is also responsible for generating required feeds when required.
 *
 */
public class FeedListPresenter extends UstadBaseController<FeedListView>{

    private long loggedInPersonUid = 0L;

    public FeedListPresenter(Object context, Hashtable arguments, FeedListView view) {
        super(context, arguments, view);
    }

    private UmProvider<FeedEntry> feedEntryUmProvider;

    private UmAppDatabase repository;

    /**
     * Overridden onCreate in order:
     *      1. Gets the UmProvider types FeedEntry list and sets it as a provider to the view.
     *
     * @param savedState    THE SAVED STATE
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();

        repository = UmAppDatabase.getInstance(view.getContext());

        updateFeedEntries();

        //All clazz's average live data
        UmLiveData<ClazzAverage> averageLiveData = repository.getClazzDao().getClazzSummaryLiveData();
        averageLiveData.observe(FeedListPresenter.this,
                FeedListPresenter.this::handleAveragesChanged);

        //Check permissions
        checkPermissions();
    }

    private void updateFeedEntries(){
        feedEntryUmProvider = repository.getFeedEntryDao().findByPersonUid(loggedInPersonUid);
        updateFeedProviderToView();
    }

    /**
     * Handles what happens when averages changes. Usually called on live data's onObserve.
     * @param average   The ClazzAverage POJO that changed.
     */
    private void handleAveragesChanged(ClazzAverage average){
        int attendanceAverage = Math.round(average.getAttendanceAverage() * 100);
        view.updateAttendancePercentage(attendanceAverage);
        view.updateNumClasses(average.getNumClazzes());
        view.updateNumStudents(average.getNumStudents());
        //TODOne: Update with attendance trend.
        //Update: Not part of new Sprint design.
        //view.updateAttendanceTrend(0, 0);

        checkPermissions();
    }

    /**
     * Checks permission and updates the view.
     */
    public void checkPermissions(){
        ClazzDao clazzDao = repository.getClazzDao();
        clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_REPORTS_VIEW,
            new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    view.showReportOptionsOnSummaryCard(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            })
        );
    }

    /**
     * Mark feed as done so it can be dismissed from the feed list.
     * @param feedUid   the feed uid
     */
    public void markFeedAsDone(long feedUid){
        FeedEntryDao feedEntryDao = repository.getFeedEntryDao();
        feedEntryDao.findByUidAsync(feedUid, new UmCallback<FeedEntry>() {
            @Override
            public void onSuccess(FeedEntry thisFeed) {
                if(thisFeed != null) {
                    thisFeed.setFeedEntryDone(true);
                    feedEntryDao.update(thisFeed);
                    updateFeedEntries();
                }
            }

            @Override
            public void onFailure(Throwable exception) { exception.printStackTrace();}
        });

    }

    /**
     * Updates the View with the feed provider set on the Presenter
     */
    private void updateFeedProviderToView(){
        view.runOnUiThread(() -> view.setFeedEntryProvider(feedEntryUmProvider));
    }

    /**
     * Splits the string query without host name and returns a hash map of it.
     *
     * @param query The string get query without host. eg: clazzuid=22&logdate=123456789
     * @return  A hash table all the query
     */
    private static Hashtable splitQuery(String query) {
        Hashtable<String, String> query_pairs = new Hashtable<>();

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return query_pairs;
    }


    /**
     * Goes to Report selection
     */
    public void handleClickViewReports(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_NAME, "Test Report");
        impl.go(ReportSelectionView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Takes action on a feed. This splits the feed's link and builds its destination and arguments
     * for it to go to.
     *
     * @param feedEntry The FeedEntry object that was clicked.
     */
    public void handleClickFeedEntry(FeedEntry feedEntry){
        String feedLink = feedEntry.getLink();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String linkViewName = feedLink.split("\\?")[0];
        Hashtable args = splitQuery(feedLink.split("\\?")[1]);
        impl.go(linkViewName, args, view.getContext());

    }

}
