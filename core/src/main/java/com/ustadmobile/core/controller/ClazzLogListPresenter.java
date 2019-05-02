package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.ThresholdResult;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClassLogListView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes;
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;
import com.ustadmobile.lib.db.entities.Role;

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
import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.STATUS_ATTENDED;

/**
 * The Presenter/Controller for ClazzLogListFragment. This is responsible for the logic behind
 * displaying every Clazz Attendance Logs via the database provider from the database/repository.
 * It is also responsible for the logic for displaying attendance charts for that class.
 */
public class ClazzLogListPresenter extends UstadBaseController<ClassLogListView>{

    private long currentClazzUid = 0L;

    private UmProvider<ClazzLogWithScheduleStartEndTimes> clazzLogListProvider;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
    private Long loggedInPersonUid;

    public ClazzLogListPresenter(Object context, Hashtable arguments, ClassLogListView view) {
        super(context, arguments, view);

        //Get clazz uid and set it
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();

        //Permissions
        checkPermissions();
    }

    public void checkPermissions(){
        ClazzDao clazzDao = repository.getClazzDao();
        clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT,
                new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                view.setFABVisibility(result);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        }));
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

        clazzLogListProvider =
                repository.getClazzLogDao().findByClazzUidNotCancelledWithSchedule(currentClazzUid);
        setProviderToView();

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
        args.put(ClassLogDetailView.ARG_CLAZZ_LOG_UID, String.valueOf(clazzLog.getClazzLogUid()));
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     *Method logic that goes to ClazzLogDetail view (take attendance) for the class we're in.
     */
    public void goToNewClazzLogDetailActivity(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        repository.getClazzLogDao().findMostRecentByClazzUid(currentClazzUid, new UmCallback<ClazzLog>() {
            @Override
            public void onSuccess(ClazzLog result) {
                if(result == null){
                    view.showMessage(MessageID.no_schedule_message);
                }else{
                    Hashtable<String, Object> args = new Hashtable<>();
                    args.put(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID,
                            String.valueOf(currentClazzUid));
                    impl.go(ClassLogDetailView.VIEW_NAME, args, view.getContext());
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    /**
     * Method that takes the duration flag and calculated daily attendance numbers for the current
     * class and updates both the line and bar charts.
     *
     * @param duration The duration flag (CHART_DURATION_LAST_WEEK, CHART_DURATION_LAST_MONTH,
     *                 CHART_DURATION_LAST_YEAR) as per defined in ClassLogListView
     */
    public void getAttendanceDataAndUpdateCharts(int duration){

        //Reset report buttons
        view.resetReportButtons();

        LinkedHashMap<Float, Float> lineDataMap = new LinkedHashMap<>();
        LinkedHashMap<Float, Float> barDataMap = new LinkedHashMap<>();
        long toDate = System.currentTimeMillis();
        long fromDate = toDate;

        switch (duration){
            case CHART_DURATION_LAST_WEEK:
                //7
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-7);
                for(int i=-7; i<-1; i++) {
                    lineDataMap.put((float) UMCalendarUtil.getDateInMilliPlusDays(i) / 1000, -1f);
                }
                break;
            case CHART_DURATION_LAST_MONTH:
                //31
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31);
                for(int i=-31; i<-1; i++){
                    lineDataMap.put((float) UMCalendarUtil.getDateInMilliPlusDays(i) / 1000, -1f);
                }
                break;
            case CHART_DURATION_LAST_YEAR:
                //31
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-365);
                for(int i=-365; i<-1; i++){
                    lineDataMap.put((float) UMCalendarUtil.getDateInMilliPlusDays(i) / 1000, -1f);
                }
                break;
            default:
                //Do nothing.
                break;
        }

        //Calculate daily attendance numbers from the database for the line chart.
        ClazzLogAttendanceRecordDao attendanceRecordDao =
                repository.getClazzLogAttendanceRecordDao();
        attendanceRecordDao.findDailyAttendanceByClazzUidAndDateAsync( currentClazzUid, fromDate,
                toDate, new UmCallback<List<DailyAttendanceNumbers>>() {
            @Override
            public void onSuccess(List<DailyAttendanceNumbers> result) {

                for(DailyAttendanceNumbers everyDayAttendance: result){
                    long dd =everyDayAttendance.getLogDate();
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
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();

        clazzMemberDao.findAttendanceSpreadByThresholdForTimePeriodAndClazzAndType(STATUS_ATTENDED,
                currentClazzUid, fromDate, toDate, new UmCallback<ThresholdResult>() {
                    @Override
                    public void onSuccess(ThresholdResult result) {
                        barDataMap.put(3f, result.getHigh()/100);
                        barDataMap.put(2f, result.getMid()/100);
                        barDataMap.put(1f, result.getLow()/100);
                        view.updateAttendanceBarChart(barDataMap);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

    }
}
