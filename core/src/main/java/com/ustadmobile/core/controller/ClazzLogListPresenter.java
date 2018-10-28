package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClassLogListView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_MONTH;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_WEEK;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_YEAR;
import static com.ustadmobile.core.view.ClazzListView.ARG_LOGDATE;

/**
 * The Presenter/Controller for ClazzLogListFragment. This is responsible in creating the provider
 * from the Dao and assigning it to the View. Any click handlers are also here.
 */
public class ClazzLogListPresenter extends UstadBaseController<ClassLogListView>{

    private long currentClazzUid = -1L;

    /**
     * Constructor to the ClazzLogList Presenter.
     * We got the class uid from the arguments passed to it.
     *
     * @param context
     * @param arguments
     * @param view
     */
    public ClazzLogListPresenter(Object context, Hashtable arguments, ClassLogListView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
    }

    /**
     * Presenter's setUiStrings method that doesn't do anything here.
     */
    @Override
    public void setUIStrings() {

    }

    private UmProvider<ClazzLog> clazzLogListProvider;

    /**
     * The Presenter here's onCreate(). This populates the provider and sets it to the View.
     *
     * This will be called when the implementation's View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        clazzLogListProvider = UmAppDatabase.getInstance(context).getClazzLogDao()
                .findByClazzUid(currentClazzUid);
        view.setClazzLogListProvider(clazzLogListProvider);

        generateAttendanceBarChartDataTest();
        generateAttendanceLineChartDataTest();

    }

    /**
     * Method logic to go to the Log Detail activity.
     * @param clazzLog
     */
    public void goToClazzLogDetailActivity(ClazzLog clazzLog){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, clazzLog.getClazzClazzUid());
        args.put(ARG_LOGDATE, clazzLog.getLogDate());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     *Method logic that goes to ClazzLogDetail view (take attendance) for the class we're in.
     */
    public void goToNewClazzLogDetailActivity(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, currentClazzUid);

        //args.put(ARG_LOGDATE, UMCalendarUtil.getDateInMilliPlusDays(0));
        args.put(ARG_LOGDATE, System.currentTimeMillis());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }

    public void getAttendanceDataAndUpdateCharts(int duration){

        LinkedHashMap<Float, Float> lineDataMap = new LinkedHashMap<>();
        LinkedHashMap<Float, Float> barDataMap = new LinkedHashMap<>();
        long toDate = System.currentTimeMillis();
        Long fromDate = toDate;

        switch (duration){
            case CHART_DURATION_LAST_WEEK:
                //7
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-7);
                for(int i=-7; i<-1; i++) {
                    lineDataMap.put((float) UMCalendarUtil.getDateInMilliPlusDays(i) / 1000, 0f);
                }
                break;
            case CHART_DURATION_LAST_MONTH:
                //31
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31);
                for(int i=-31; i<-1; i++){
                    lineDataMap.put((float) UMCalendarUtil.getDateInMilliPlusDays(i) / 1000, 0f);
                }
                break;
            case CHART_DURATION_LAST_YEAR:
                //31
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-365);
                for(int i=-365; i<-1; i++){
                    lineDataMap.put((float) UMCalendarUtil.getDateInMilliPlusDays(i) / 1000, 0f);
                }
                break;
            default:
                //Do nothing.
                break;
        }
        ClazzLogAttendanceRecordDao attendanceRecordDao =
                UmAppDatabase.getInstance(context).getClazzLogAttendanceRecordDao();
        attendanceRecordDao.findDailyAttendanceByClazzUidAndDateAsync(currentClazzUid, fromDate, toDate,
                new UmCallback<List<DailyAttendanceNumbers>>() {
            @Override
            public void onSuccess(List<DailyAttendanceNumbers> result) {

                float attendanceGreenTotal = 0f;
                float attendanceOrangeTotal = 0f;
                float attendanceRedTotal = 0f;

                for(DailyAttendanceNumbers everyDayAttendance: result){
                    Long dd =everyDayAttendance.getLogDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dd);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Long d = calendar.getTimeInMillis();
                    float a = everyDayAttendance.getAttendancePercentage();
                    lineDataMap.put(d.floatValue() / 1000, (float) a);
                    if(everyDayAttendance.getAttendancePercentage() > 0.79){
                        attendanceGreenTotal += everyDayAttendance.getAttendancePercentage();
                    }else if(everyDayAttendance.getAttendancePercentage() > 0.59){
                        attendanceOrangeTotal += everyDayAttendance.getAttendancePercentage();
                    }else{
                        attendanceRedTotal += everyDayAttendance.getAttendancePercentage();
                    }

                }

                //Remove messy date keys
                Iterator<Map.Entry<Float, Float>> ldpi = lineDataMap.entrySet().iterator();
                LinkedHashMap<Float, Float> lineDataMapFixedX = new LinkedHashMap<>();
                float l = 0f;
                while(ldpi.hasNext()){
                    l++;
                    lineDataMapFixedX.put(l, ldpi.next().getValue());
                }

                view.updateAttendanceLineChart(lineDataMapFixedX);

                barDataMap.put(3f, attendanceGreenTotal/ result.size());
                barDataMap.put(2f, attendanceOrangeTotal/result.size());
                barDataMap.put(1f, attendanceRedTotal/result.size());
                view.updateAttendanceBarChart(barDataMap);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }


    public void generateAttendanceLineChartDataTest(){
        LinkedHashMap<Float, Float> lineData = new LinkedHashMap<>();
        lineData.put(1f, 0.1f);
        lineData.put(2f, 0.4f);
        lineData.put(3f, 0.2f);
        lineData.put(4f, 0.4f);
        lineData.put(5f, 0.2f);
        lineData.put(6f, 0.4f);
        lineData.put(7f, 0.2f);


        view.updateAttendanceLineChart(lineData);
    }

    public void generateAttendanceBarChartDataTest(){

        LinkedHashMap<Float, Float> barData = new LinkedHashMap<>();
        for(float i=1; i<4; i++){
            barData.put(i, 0.3f*i);
        }
        view.updateAttendanceBarChart(barData);
    }

    /**
     * Method logic for what happens when we change the order of the log list
     *
     * @param order The order flag. 1 - Attendance, 2 - Date
     */
    public void handleChangeSortOrder(int order){
        //TODO: Change provider's sort order
    }


}
