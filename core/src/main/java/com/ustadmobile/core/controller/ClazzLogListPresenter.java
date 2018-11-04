package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClassLogListView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_MONTH;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_WEEK;
import static com.ustadmobile.core.view.ClassLogListView.CHART_DURATION_LAST_YEAR;
import static com.ustadmobile.core.view.ClazzListView.ARG_LOGDATE;

/**
 * The Presenter/Controller for ClazzLogListFragment. This is responsible for the logic behind
 * displaying every Clazz Attendance Logs via the database provider from the database/repository.
 * It is also responsible for the logic for displaying attendance charts for that class.
 */
public class ClazzLogListPresenter extends UstadBaseController<ClassLogListView>{

    private long currentClazzUid = -1L;

    private UmProvider<ClazzLog> clazzLogListProvider;

    public ClazzLogListPresenter(Object context, Hashtable arguments, ClassLogListView view) {
        super(context, arguments, view);

        //Get clazz uid and set it
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
    }


    /**
     * In Order:
     *      1. This populates the Attendance/Log Entry list provider and sets it to the View.
     *      2. Generate attendance line chart
     *      3. Generate attendance bar chart
     *
     * This will be called when the implementation's View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState    The savedState
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        clazzLogListProvider = UmAppDatabase.getInstance(context).getClazzLogDao()
                .findByClazzUid(currentClazzUid);
        setProviderToView();

        generateAttendanceBarChartDataTest();
        generateAttendanceLineChartDataTest();

    }

    /**
     * Sets the clazz log list / clazz attendance list UMProvider provider set in this Presenter
     * to the View object.
     */
    private void setProviderToView(){
        view.setClazzLogListProvider(clazzLogListProvider);
    }

    /**
     * Method logic to go to the Log Detail activity - To see the attendance entry details and edit
     * them.
     *
     * @param clazzLog  The clazz log entry uid that we want to edit.
     */
    public void goToClazzLogDetailActivity(ClazzLog clazzLog){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, clazzLog.getClazzClazzUid());
        args.put(ARG_LOGDATE, clazzLog.getLogDate());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     *Method logic that goes to ClazzLogDetail view (take attendance) for the class we're in.
     */
    public void goToNewClazzLogDetailActivity(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_LOGDATE, System.currentTimeMillis());
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     * Method that takes the duration flag and calculated daily attendance numbers for the current
     * class and updates both the line and bar charts.
     *
     * @param duration The duration flag (CHART_DURATION_LAST_WEEK, CHART_DURATION_LAST_MONTH,
     *                 CHART_DURATION_LAST_YEAR) as per defined in ClassLogListView
     */
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

        //Calculate daily attendance numbers from the database for the line chart.
        ClazzLogAttendanceRecordDao attendanceRecordDao =
                UmAppDatabase.getInstance(context).getClazzLogAttendanceRecordDao();
        attendanceRecordDao.findDailyAttendanceByClazzUidAndDateAsync(currentClazzUid, fromDate, toDate,
                new UmCallback<List<DailyAttendanceNumbers>>() {
            @Override
            public void onSuccess(List<DailyAttendanceNumbers> result) {



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
                    lineDataMap.put(d.floatValue() / 1000, a);

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

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        //Calculate attendance average numbers for the bar chart.
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();
        clazzMemberDao.getAttendanceAverageAsListForClazzBetweenDates(currentClazzUid, fromDate,
                toDate, new UmCallback<List<Float>>() {
                    @Override
                    public void onSuccess(List<Float> result) {

                        float attendanceGreenTotal = 0f;
                        float attendanceOrangeTotal = 0f;
                        float attendanceRedTotal = 0f;


                        for(Float everyValue: result){
                            if(everyValue > 0.79){
                                attendanceGreenTotal += everyValue;
                            }else if(everyValue > 0.59){
                                attendanceOrangeTotal += everyValue;
                            }else{
                                attendanceRedTotal += everyValue;
                            }
                        }

                        barDataMap.put(3f, attendanceGreenTotal/ result.size());
                        barDataMap.put(2f, attendanceOrangeTotal/result.size());
                        barDataMap.put(1f, attendanceRedTotal/result.size());
                        view.updateAttendanceBarChart(barDataMap);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }


    /**
     * Generate and calculate test data for the line chart. TODO: Remove for production
     */
    private void generateAttendanceLineChartDataTest(){
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

    /**
     * Generate and calculate test data for the bar chart. TODO: Remove for production.
     */
    private void generateAttendanceBarChartDataTest(){

        LinkedHashMap<Float, Float> barData = new LinkedHashMap<>();

        barData.put(3f, 0.9f);
        barData.put(2f, 0.6f);
        barData.put(1f, 0.3f);
        view.updateAttendanceBarChart(barData);
    }

//    /**
//     * Method logic for what happens when we change the order of the log list
//     *
//     * @param order The order flag. 1 - Attendance, 2 - Date
//     */
//    public void handleChangeSortOrder(int order){
//        //TODO: Change provider's sort order
//    }

    /**
     * Presenter's setUiStrings method that doesn't do anything here.
     */
    @Override
    public void setUIStrings() {

    }


}
