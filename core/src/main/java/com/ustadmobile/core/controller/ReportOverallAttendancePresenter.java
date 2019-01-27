package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportOverallAttendanceView;
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_STUDENT_IDENTIFIER_NUMBER;
import static com.ustadmobile.core.view.ReportEditView.ARG_STUDENT_IDENTIFIER_PERCENTAGE;
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
    private long[] locations;
    private List<Long> locationList;
    private long[] clazzes;
    private List<Long> clazzesList;
    private boolean genderDisaggregate;
    private Boolean showPercentages = false;

    LinkedHashMap<String, LinkedHashMap<Float, Float>> dataMaps;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);


    public boolean isGenderDisaggregate() {
        return genderDisaggregate;
    }

    public void setGenderDisaggregate(boolean genderDisaggregate) {
        this.genderDisaggregate = genderDisaggregate;
    }

    public static ArrayList<Long> convertLongArray(long[] array) {
        ArrayList<Long> result = new ArrayList<Long>(array.length);
        for (long item : array)
            result.add(item);
        return result;
    }

    public static Long[] convertLongList(List<Long> list){
        Long[] array = new Long[list.size()];
        int i=0;
        for(Long everyList:list){
            array[i] = everyList;
            i++;
        }
        return array;
    }

    public ReportOverallAttendancePresenter(Object context, Hashtable arguments,
                                            ReportOverallAttendanceView view) {
        super(context, arguments, view);

        locationList = new ArrayList<>();
        clazzesList = new ArrayList<>();

        if(arguments.containsKey(ARG_FROM_DATE)){
            fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            toDate = (long) arguments.get(ARG_TO_DATE);
        }
        if(arguments.containsKey(ARG_LOCATION_LIST)){
            locations = (long[]) arguments.get(ARG_LOCATION_LIST);
            locationList = convertLongArray(locations);
        }
        if(arguments.containsKey(ARG_CLAZZ_LIST)){
            clazzes = (long[]) arguments.get(ARG_CLAZZ_LIST);
            clazzesList = convertLongArray(clazzes);
        }
        if(arguments.containsKey(ARG_GENDER_DISAGGREGATE)){
            genderDisaggregate = (Boolean) arguments.get(ARG_GENDER_DISAGGREGATE);
        }

        if(arguments.containsKey(ARG_STUDENT_IDENTIFIER_NUMBER)){
            Boolean numberIdentifier = (Boolean) arguments.get(ARG_STUDENT_IDENTIFIER_NUMBER);
            if (numberIdentifier){
                setShowPercentages(false);
            }else{
                setShowPercentages(true);
            }
        }

        if(arguments.containsKey(ARG_STUDENT_IDENTIFIER_PERCENTAGE)){
            Boolean percentageIdentifier = (Boolean) arguments.get(ARG_STUDENT_IDENTIFIER_PERCENTAGE);
            if(percentageIdentifier){
                showPercentages = true;
            }else{
                showPercentages = false;
            }
        }
    }

    public Boolean getShowPercentages() {
        return showPercentages;
    }

    public void setShowPercentages(Boolean showPercentages) {
        this.showPercentages = showPercentages;
    }

    private void processDailyAttendanceNumbers(List<DailyAttendanceNumbers> result){
        LinkedHashMap<Float, Float> lineDataMap = new LinkedHashMap<>();
        LinkedHashMap<Float, Float> lineDataMapMale = new LinkedHashMap<>();
        LinkedHashMap<Float, Float> lineDataMapFemale = new LinkedHashMap<>();

        LinkedHashMap<String, LinkedHashMap<String, Float>> tableData = new LinkedHashMap<>();

        LinkedHashMap<String, Float> tableDataAverage = new LinkedHashMap<>();
        LinkedHashMap<String, Float> tableDataMale = new LinkedHashMap<>();
        LinkedHashMap<String, Float> tableDataFemale = new LinkedHashMap<>();

        for(DailyAttendanceNumbers everyDayAttendance: result){

            //Get date and time.
            Long dd =everyDayAttendance.getLogDate();

            //Remove time and just get date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dd);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Long d = calendar.getTimeInMillis();

            //Put just date and attendance value
            lineDataMap.put(d.floatValue() / 1000,
                    everyDayAttendance.getAttendancePercentage());
            lineDataMapMale.put(d.floatValue() / 1000,
                    everyDayAttendance.getMaleAttendance());
            lineDataMapFemale.put(d.floatValue() / 1000,
                    everyDayAttendance.getFemaleAttendance());

            tableDataAverage.put(
                    UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, Locale.US),
                    everyDayAttendance.getAttendancePercentage() * 100);
            tableDataMale.put(
                    UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, Locale.US),
                    everyDayAttendance.getMaleAttendance() * 100);
            tableDataFemale.put(
                    UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, Locale.US),
                    everyDayAttendance.getFemaleAttendance() * 100);

        }

        tableData.put(ATTENDANCE_LINE_AVERAGE_LABEL_DESC, tableDataAverage);
        tableData.put(ATTENDANCE_LINE_MALE_LABEL_DESC, tableDataMale);
        tableData.put(ATTENDANCE_LINE_FEMALE_LABEL_DESC, tableDataFemale);


        dataMaps = new LinkedHashMap<>();

        if(genderDisaggregate) {
            dataMaps.put(ATTENDANCE_LINE_MALE_LABEL_DESC, lineDataMapMale);
            dataMaps.put(ATTENDANCE_LINE_FEMALE_LABEL_DESC, lineDataMapFemale);
            dataMaps.put(ATTENDANCE_LINE_AVERAGE_LABEL_DESC, lineDataMap);
        }else{
            dataMaps.put(ATTENDANCE_LINE_AVERAGE_LABEL_DESC, lineDataMap);
        }

        view.updateAttendanceMultiLineChart(dataMaps, tableData);
    }
    public void getAttendanceDataAndUpdateCharts(){


        ClazzLogAttendanceRecordDao attendanceRecordDao =
                repository.getClazzLogAttendanceRecordDao();

        //TODO: Account for locations in the Dao.
        attendanceRecordDao.findOverallDailyAttendanceNumbersByDateAndStuff(fromDate, toDate,
            clazzesList, locationList, new UmCallback<List<DailyAttendanceNumbers>>() {

                @Override
                public void onSuccess(List<DailyAttendanceNumbers> result) {
                    processDailyAttendanceNumbers(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

    }

    public void dataToCSV(){
        view.generateCSVReport();
    }

    public void dataToXLS(){
        //TODO
    }

    public void dataToJSON(){
        //TODO
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        getAttendanceDataAndUpdateCharts();
    }

    @Override
    public void setUIStrings() {

    }

}
