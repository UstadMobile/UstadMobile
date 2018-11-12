package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClazzActivityListView;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.entities.DailyActivityNumbers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_MONTH;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_WEEK;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_YEAR;


/**
 * The ClazzActivityList Presenter - Responsible for the logic of Activity Tab in Clazz Detail.
 * ie: Showing Activity List on the view and showing Activity bar charts as well.
 *
 */
public class ClazzActivityListPresenter
        extends UstadBaseController<ClazzActivityListView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long clazzActivityChangeUid = -1;

    private HashMap<Float, Long> changeToIdMap;
    private HashMap<Float, Long> barMapWithOGDateTimes;

    //Provider 
    private UmProvider<ClazzActivity> providerList;

    UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
    private ClazzActivityDao clazzActivityDao =
            UmAppDatabase.getInstance(context).getClazzActivityDao();
    private ClazzActivityChangeDao activityChangeDao =
            UmAppDatabase.getInstance(context).getClazzActivityChangeDao();


    public ClazzActivityListPresenter(Object context, Hashtable arguments, ClazzActivityListView view) {
        super(context, arguments, view);

        //Get Clazz Uid and save it.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    /**
     * Method to update the Activity Change options in the view for the bar chart.
     */
    private void updateChangeOptions(){

        activityChangeDao.findAllClazzActivityChangesAsync(
                new UmCallback<List<ClazzActivityChange>>() {
            @Override
            public void onSuccess(List<ClazzActivityChange> result) {
                changeToIdMap = new HashMap<>();
                ArrayList<String> presetAL = new ArrayList<>();
                float i=0;
                for(ClazzActivityChange everyChange: result){
                    presetAL.add(everyChange.getClazzActivityChangeTitle());

                    // Save mapping of position and activity change's uid to handle it.
                    changeToIdMap.put(i, everyChange.getClazzActivityChangeUid());
                    i++;
                }
                Object[] objectArr = presetAL.toArray();
                String[] strArr = new String[objectArr.length];
                for(int j = 0 ; j < objectArr.length ; j ++){
                    strArr[j] = (String) objectArr[j];
                }
                view.setClazzActivityChangesDropdownPresets(strArr);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Order:
     *      1. Get all the activity for the current clazz.
     *      2. Set the provider to the view
     *      3. Update Activity Change presets from Database
     *      4. Populate chart.
     *
     * @param savedState    The savedState
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = UmAppDatabase.getInstance(context).getClazzActivityDao()
                .findByClazzUid(currentClazzUid);

        setProviderOnView();

        //Update Change options
        updateChangeOptions();

        //Load test data for now TODO: remove in production.
        generateActivityBarChartDataTest();

    }

    private void setProviderOnView(){
        //set Provider.
        view.setListProvider(providerList);
    }

    /**
     * Sets the current Activity Change Uid for the charts.
     * @param clazzActivityChangeUid The Activity Change Uid to be set to this presenter
     *                               (usually for charts)
     */
    public void setClazzActivityChangeUid(long clazzActivityChangeUid) {
        if(changeToIdMap != null) {
            if(changeToIdMap.containsKey((float)clazzActivityChangeUid)) {
                this.clazzActivityChangeUid = changeToIdMap.get((float)clazzActivityChangeUid);
            }
        }
    }

    /**
     * Generates test data for the Bar chart. TODO: Remove in production
     */
    private void generateActivityBarChartDataTest(){

        LinkedHashMap<Float, Float> barData = new LinkedHashMap<>();

        barData.put(1f, 5f);
        barData.put(2f,  -2f);
        barData.put(3f, 5f);
        barData.put(4f, -2f);
        barData.put(5f, -2f);
        view.updateActivityBarChart(barData);
    }

    /**
     * Handles going to Creating a new Activity. This is to be called when "Record Activity" FAB is
     * clicked.
     */
    public void goToNewClazzActivityEditActivity(){
        /* TODO Sprint 4
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_LOGDATE, System.currentTimeMillis());

        impl.go(ClazzActivityEditView.VIEW_NAME, args, view.getContext());
        */
    }

    /**
     * The common method to update the Activity Bar chart based on the duration given to it.
     * The Activity Change is already set from this Presenter's setters.
     * The method queries the database and gets a daily aggregate for that clazz based on the
     * duration given to it (WEEK/MONTH/YEAR).
     *
     * @param duration The duration constant that is defined in ClazzLogListView for WEEK, MONTH,
     *                 YEAR.
     */
    public void getActivityDataAndUpdateCharts(int duration){

        //Reset the report buttons
        view.resetReportButtons();

        LinkedHashMap<Float, Float> barDataMap = new LinkedHashMap<>();
        long toDate = System.currentTimeMillis();
        Long fromDate = toDate;
        int groupOffset=1;

        switch (duration){
            case CHART_DURATION_LAST_WEEK:
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-7);
                break;
            case CHART_DURATION_LAST_MONTH:
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31);
                groupOffset = 7;
                break;
            case CHART_DURATION_LAST_YEAR:
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-365);
                groupOffset = 30;
                break;
            default:
                //Do nothing.
                break;
        }

        //Get aggregate daily data about Clazz Activity.
        int finalGroupOffset = groupOffset;
        clazzActivityDao.getDailyAggregateFeedbackByActivityChange(currentClazzUid, fromDate,
                toDate,clazzActivityChangeUid, new UmCallback<List<DailyActivityNumbers>>() {
                    @Override
                    public void onSuccess(List<DailyActivityNumbers> result) {

                        LinkedHashMap<Float, Float> barDataMapGrouped = new LinkedHashMap<>();

                        barMapWithOGDateTimes = new HashMap<>();
                        float f = 0f;
                        float h=1f;
                        float g=0f;

                        int goodGrouped = 0;
                        int badGrouped = 0;

                        for(DailyActivityNumbers everyDayAttendance: result){

                            f++;
                            //h++;

                            int good = everyDayAttendance.getGood();
                            int bad = everyDayAttendance.getBad();
                            long thisDate = everyDayAttendance.getDayDate();

                            barMapWithOGDateTimes.put(f, thisDate);

                            //Sum up the good and bad for the day
                            if(good>bad) {
                                barDataMap.put(f, (float) good);
                            }else{
                                barDataMap.put(f, -(float)bad);
                            }

                            if(h <= finalGroupOffset){
                                goodGrouped = goodGrouped + good;
                                badGrouped = badGrouped + bad;

                                h++;
                                if(h>finalGroupOffset){
                                    g++;

                                    if(goodGrouped>badGrouped) {
                                        barDataMapGrouped.put( g, (float) goodGrouped);
                                    }else{
                                        barDataMapGrouped.put( g, (float) -(badGrouped));
                                    }

                                    h=1;
                                    goodGrouped=0;
                                    badGrouped = 0;
                                }
                            }
                        }

                        //view.updateActivityBarChart(barDataMap);
                        view.updateActivityBarChart(barDataMapGrouped);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    @Override
    public void setUIStrings() {

    }

}
