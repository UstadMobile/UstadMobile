package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ReportOverallAttendanceView;
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;
import static com.ustadmobile.core.view.ReportOverallAttendanceView.ATTENDANCE_LINE_AVERAGE_LABEL_DESC;
import static com.ustadmobile.core.view.ReportOverallAttendanceView.ATTENDANCE_LINE_FEMALE_LABEL_DESC;
import static com.ustadmobile.core.view.ReportOverallAttendanceView.ATTENDANCE_LINE_MALE_LABEL_DESC;


/**
 * The ReportOverallAttendance Presenter.
 */
public class ReportOverallAttendancePresenter
        extends UstadBaseController<ReportOverallAttendanceView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;
    private long fromDate;
    private long toDate;
    private Long[] locations;
    private Long[] clazzes;
    private boolean genderDisaggregate;

    LinkedHashMap<String, LinkedHashMap<Float, Float>> dataMaps;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);


    public boolean isGenderDisaggregate() {
        return genderDisaggregate;
    }

    public void setGenderDisaggregate(boolean genderDisaggregate) {
        this.genderDisaggregate = genderDisaggregate;
    }

    public ReportOverallAttendancePresenter(Object context, Hashtable arguments,
                                            ReportOverallAttendanceView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_FROM_DATE)){
            fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            toDate = (long) arguments.get(ARG_TO_DATE);
        }
        if(arguments.containsKey(ARG_LOCATION_LIST)){
            locations = (Long[]) arguments.get(ARG_LOCATION_LIST);
        }
        if(arguments.containsKey(ARG_CLAZZ_LIST)){
            clazzes = (Long[]) arguments.get(ARG_CLAZZ_LIST);
        }
        if(arguments.containsKey(ARG_GENDER_DISAGGREGATE)){
            genderDisaggregate = (Boolean) arguments.get(ARG_GENDER_DISAGGREGATE);
        }

    }

    public void getAttendanceDataAndUpdateCharts(){
        LinkedHashMap<Float, Float> lineDataMap = new LinkedHashMap<>();
        LinkedHashMap<Float, Float> lineDataMapMale = new LinkedHashMap<>();
        LinkedHashMap<Float, Float> lineDataMapFemale = new LinkedHashMap<>();

        ClazzLogAttendanceRecordDao attendanceRecordDao =
                repository.getClazzLogAttendanceRecordDao();
        attendanceRecordDao.findOverallDailyAttendanceNumbersByDateAndStuff(fromDate,
                toDate, new UmCallback<List<DailyAttendanceNumbers>>() {
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

                            lineDataMapMale.put(d.floatValue() / 1000, everyDayAttendance.getMaleAttendance());
                            lineDataMapFemale.put(d.floatValue() / 1000, everyDayAttendance.getFemaleAttendance());


                        }


                        //Remove messy date keys
                        Iterator<Map.Entry<Float, Float>> ldpi = lineDataMap.entrySet().iterator();
                        Iterator<Map.Entry<Float, Float>> ldpiMale = lineDataMapMale.entrySet().iterator();
                        Iterator<Map.Entry<Float, Float>> ldpiFemale = lineDataMapFemale.entrySet().iterator();

                        LinkedHashMap<Float, Float> lineDataMapFixedX = new LinkedHashMap<>();
                        LinkedHashMap<Float, Float> lineDataMapFixedXMale = new LinkedHashMap<>();
                        LinkedHashMap<Float, Float> lineDataMapFixedXFemale = new LinkedHashMap<>();

                        float l = 0f;
                        while(ldpi.hasNext()){
                            l++;
                            lineDataMapFixedX.put(l, ldpi.next().getValue());

                        }
                        l = 0f;
                        while(ldpiMale.hasNext()){
                            l++;
                            lineDataMapFixedXMale.put(l, ldpiMale.next().getValue());

                        }
                        l = 0f;
                        while(ldpiFemale.hasNext()){
                            l++;
                            lineDataMapFixedXFemale.put(l, ldpiFemale.next().getValue());

                        }

                        dataMaps = new LinkedHashMap<>();

                        if(genderDisaggregate) {
                            dataMaps.put(ATTENDANCE_LINE_MALE_LABEL_DESC, lineDataMapFixedXMale);
                            dataMaps.put(ATTENDANCE_LINE_FEMALE_LABEL_DESC, lineDataMapFixedXFemale);
                            dataMaps.put(ATTENDANCE_LINE_AVERAGE_LABEL_DESC, lineDataMapFixedX);
                        }else{
                            dataMaps.put(ATTENDANCE_LINE_AVERAGE_LABEL_DESC, lineDataMapFixedX);
                        }

                        //view.updateAttendanceLineChart(lineDataMapFixedX);

                        view.updateAttendanceMultiLineChart(dataMaps);

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    public void dataToCSV(){

    }

    public void dataToXLS(){

    }

    public void dataToJSON(){

    }



    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        getAttendanceDataAndUpdateCharts();
    }

    public void handleClickPrimaryActionButton(long selectedObjectUid) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create arguments
        Hashtable args = new Hashtable();
        //eg: args.put(ARG_CLAZZ_UID, selectedObjectUid);

        //Go to view
        //eg: impl.go(SELEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void setUIStrings() {

    }

}
