package com.ustadmobile.core.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClazzActivityEditView;
import com.ustadmobile.core.view.ClazzActivityListView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.entities.DailyActivityNumbers;
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_MONTH;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_WEEK;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_YEAR;
import static com.ustadmobile.core.view.ClazzListView.ARG_LOGDATE;


/**
 * The ClazzActivityList Presenter.
 */
public class ClazzActivityListPresenter
        extends UstadBaseController<ClazzActivityListView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long clazzActivityChangeUid = -1;

    private HashMap<Float, Long> changeToIdMap;
    private HashMap<Float, Long> barMapWithOGDateTimes;
    //Provider 
    UmProvider<ClazzActivity> providerList;

    UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
    ClazzDao clazzDao = UmAppDatabase.getInstance(getContext()).getClazzDao();
    ClazzActivityDao clazzActivityDao =
            UmAppDatabase.getInstance(context).getClazzActivityDao();
    ClazzActivityChangeDao activityChangeDao =
            UmAppDatabase.getInstance(context).getClazzActivityChangeDao();


    public ClazzActivityListPresenter(Object context, Hashtable arguments, ClazzActivityListView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    public void updateChangeOptions(){

        activityChangeDao.findAllClazzActivityChangesAsync(new UmCallback<List<ClazzActivityChange>>() {
            @Override
            public void onSuccess(List<ClazzActivityChange> result) {
                changeToIdMap = new HashMap<>();
                ArrayList<String> presetAL = new ArrayList<String>();
                float i=0;
                for(ClazzActivityChange everyChange: result){

                    presetAL.add(everyChange.getClazzActivityChangeTitle());
                    //TODO: any way to send id as well ?
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

            }
        });
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //TODO: check
        //Populate the provider
        providerList = UmAppDatabase.getInstance(context).getClazzActivityDao()
                .findByClazzUid(currentClazzUid);
        //set Provider.
        view.setListProvider(providerList);

        //Update Change options
        updateChangeOptions();

        //Load test data for now TODO: remove in production.
        generateActivityBarChartDataTest();

    }

    public long getClazzActivityChangeUid() {
        return clazzActivityChangeUid;
    }

    public void setClazzActivityChangeUid(long clazzActivityChangeUid) {
        if(changeToIdMap != null) {
            if(changeToIdMap.containsKey((float)clazzActivityChangeUid)) {
                long actualChangeUid = changeToIdMap.get((float)clazzActivityChangeUid);
                this.clazzActivityChangeUid = actualChangeUid;
            }
        }
    }

    public void generateActivityBarChartDataTest(){

        LinkedHashMap<Float, Float> barData = new LinkedHashMap<>();

        barData.put(1f, 5f);
        barData.put(2f,  -2f);
        barData.put(3f, 5f);
        barData.put(4f, -2f);
        barData.put(5f, -2f);
        view.updateActivityBarChart(barData);
    }

    public void goToNewClazzActivityEditActivity(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_LOGDATE, System.currentTimeMillis());

        impl.go(ClazzActivityEditView.VIEW_NAME, args, view.getContext());
    }

    public void getActivityDataAndUpdateCharts(int duration){

        LinkedHashMap<Float, Float> barDataMap = new LinkedHashMap<>();
        long toDate = System.currentTimeMillis();
        Long fromDate = toDate;

        switch (duration){
            case CHART_DURATION_LAST_WEEK:
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-7);
                break;
            case CHART_DURATION_LAST_MONTH:
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31);
                break;
            case CHART_DURATION_LAST_YEAR:
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-365);
                break;
            default:
                //Do nothing.
                break;
        }

        clazzActivityDao.getDailyAggregateFeedbackByActivityChange(currentClazzUid, fromDate,
                toDate,clazzActivityChangeUid, new UmCallback<List<DailyActivityNumbers>>() {
                    @Override
                    public void onSuccess(List<DailyActivityNumbers> result) {

                        barMapWithOGDateTimes = new HashMap<>();
                        float f = 0f;
                        for(DailyActivityNumbers everyDayAttendance: result){

                            f++;

                            int good = everyDayAttendance.getGood();
                            int bad = everyDayAttendance.getBad();
                            long thisDate = everyDayAttendance.getDayDate();

                            barMapWithOGDateTimes.put(f, thisDate);

                            if(good>bad) {
                                barDataMap.put(f, (float) good);
                            }else{
                                barDataMap.put(f, -(float)bad);
                            }

                        }

                        view.updateActivityBarChart(barDataMap);
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });


    }

    @Override
    public void setUIStrings() {

    }

}
