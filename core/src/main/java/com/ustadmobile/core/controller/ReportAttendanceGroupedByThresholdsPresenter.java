package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ReportAttendanceGroupedByThresholdsView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao.AttendanceResultGroupedByAgeAndThreshold;
import com.ustadmobile.lib.db.entities.Location;

import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_HIGH;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_LOW;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_MID;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;


/**
 * The ReportNumberOfDaysClassesOpen Presenter.
 */
public class ReportAttendanceGroupedByThresholdsPresenter
        extends UstadBaseController<ReportAttendanceGroupedByThresholdsView> {

    private long fromDate;
    private long toDate;
    private List<Long> clazzList, locationList;
    private ThresholdValues thresholdValues;
    private int index;

    LinkedHashMap<String, List<AttendanceResultGroupedByAgeAndThreshold>> dataMapsMap;

    UmAppDatabase repository;

    public static class ThresholdValues{
        public int low, med, high;
    }

    public ThresholdValues getThresholdValues() {
        return thresholdValues;
    }

    public void setThresholdValues(ThresholdValues thresholdValues) {
        this.thresholdValues = thresholdValues;
    }

    private static ArrayList<Long> convertLongArray(long[] array) {
        ArrayList<Long> result = new ArrayList<Long>(array.length);
        for (long item : array)
            result.add(item);
        return result;
    }

    public ReportAttendanceGroupedByThresholdsPresenter(Object context, Hashtable arguments,
                                                ReportAttendanceGroupedByThresholdsView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        dataMapsMap = new LinkedHashMap<>();
        index = 0;

        thresholdValues = new ThresholdValues();
        clazzList = new ArrayList<>();
        locationList = new ArrayList<>();

        if(arguments.containsKey(ARG_FROM_DATE)){
            fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            toDate = (long) arguments.get(ARG_TO_DATE);
        }

        if(arguments.containsKey(ARG_LOCATION_LIST)){
            long[] locations = (long[]) arguments.get(ARG_LOCATION_LIST);
            locationList = convertLongArray(locations);
        }
        if(arguments.containsKey(ARG_CLAZZ_LIST)){
            long[] clazzes = (long[]) arguments.get(ARG_CLAZZ_LIST);
            clazzList = convertLongArray(clazzes);
        }

        if(arguments.containsKey(ARG_THRESHOLD_LOW)){
            thresholdValues.low = (int) arguments.get(ARG_THRESHOLD_LOW);
        }
        if(arguments.containsKey(ARG_THRESHOLD_MID)){
            thresholdValues.med = (int) arguments.get(ARG_THRESHOLD_MID);
        }
        if(arguments.containsKey(ARG_THRESHOLD_HIGH)){
            thresholdValues.high = (int) arguments.get(ARG_THRESHOLD_HIGH);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        getDataAndUpdateTable();
    }

    private void buildMapAndUpdateView(String theLocationName,
                                       List<AttendanceResultGroupedByAgeAndThreshold> result){
        dataMapsMap.put(theLocationName, result);
        if(index >= locationList.size()){
            view.updateTables(dataMapsMap);
        }
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private void getDataAndUpdateTable(){

        long currentTime = System.currentTimeMillis();
        ClazzLogAttendanceRecordDao recordDao = repository.getClazzLogAttendanceRecordDao();
        LocationDao locationdao = repository.getLocationDao();

        //Loop over locations
        if(!locationList.isEmpty()){
            for(Long locationUid : locationList){


                locationdao.findByUidAsync(locationUid, new UmCallback<Location>() {
                    @Override
                    public void onSuccess(Location theLocation) {
                        String theLocationName = theLocation.getTitle();

                        if(!clazzList.isEmpty()){
                            recordDao.getAttendanceGroupedByThresholds(currentTime, fromDate, toDate,
                                    thresholdValues.low, thresholdValues.med,
                                    clazzList, locationUid,
                                    new UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>>() {
                                        @Override
                                        public void onSuccess(List<AttendanceResultGroupedByAgeAndThreshold> result) {
                                            index++;
                                            buildMapAndUpdateView(theLocationName, result);
                                        }

                                        @Override
                                        public void onFailure(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    });
                        }else{
                            recordDao.getAttendanceGroupedByThresholds(currentTime, fromDate, toDate,
                                    thresholdValues.low, thresholdValues.med,
                                    locationUid,
                                    new UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>>() {
                                        @Override
                                        public void onSuccess(List<AttendanceResultGroupedByAgeAndThreshold> result) {
                                            index++;
                                            buildMapAndUpdateView(theLocationName, result);
                                        }

                                        @Override
                                        public void onFailure(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

            }

        }else {

            String overallLocation = "Overall";
            recordDao.getAttendanceGroupedByThresholds(
                    currentTime, fromDate, toDate, thresholdValues.low, thresholdValues.med,
                    clazzList, locationList,
                    new UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>>() {

                        @Override
                        public void onSuccess(List<AttendanceResultGroupedByAgeAndThreshold> result) {

                            dataMapsMap.put(overallLocation, result);

                            view.updateTables(dataMapsMap);

                        }

                        @Override
                        public void onFailure(Throwable exception) {

                        }
                    });
        }

    }

    //TODO: Export

    public void dataToCSV(){
        view.generateCSVReport();
    }

    public void dataToXLS(){

    }

    public void dataToJSON(){

    }

    @Override
    public void setUIStrings() {

    }

}
