package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ReportAttendanceGroupedByThresholdsView;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao.AttendanceResultGroupedByAgeAndThreshold;

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
    private long[] locations;
    private long[] clazzes;
    private ThresholdValues thresholdValues;

    public static class ThresholdValues{
        public int low, med, high;
    }


    public ThresholdValues getThresholdValues() {
        return thresholdValues;
    }

    public void setThresholdValues(ThresholdValues thresholdValues) {
        this.thresholdValues = thresholdValues;
    }
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public ReportAttendanceGroupedByThresholdsPresenter(Object context, Hashtable arguments,
                                                        ReportAttendanceGroupedByThresholdsView view) {
        super(context, arguments, view);

        thresholdValues = new ThresholdValues();

        if(arguments.containsKey(ARG_FROM_DATE)){
            fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            toDate = (long) arguments.get(ARG_TO_DATE);
        }

        if(arguments.containsKey(ARG_LOCATION_LIST)){
            locations = (long[]) arguments.get(ARG_LOCATION_LIST);
        }
        if(arguments.containsKey(ARG_CLAZZ_LIST)){
            clazzes = (long[]) arguments.get(ARG_CLAZZ_LIST);
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

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private void getDataAndUpdateTable(){

        LinkedHashMap<Float, Float> dataMap = new LinkedHashMap<>();

        //TODO: Account for location and clazzes.

        //TODO: Loop through locations

        LinkedHashMap<String, List<AttendanceResultGroupedByAgeAndThreshold>> dataMapsMap =
                new LinkedHashMap<>();

        String locationSet = "Overall";
        repository.getClazzLogAttendanceRecordDao()
                .getAttendanceGroupedByThresholds(System.currentTimeMillis(),
                        fromDate, toDate, thresholdValues.low, thresholdValues.med,
                        new UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>>() {
                            @Override
                            public void onSuccess(List<AttendanceResultGroupedByAgeAndThreshold> result) {

                                dataMapsMap.put(locationSet, result);

                                view.updateTables(dataMapsMap);
                            }

                            @Override
                            public void onFailure(Throwable exception) {

                            }
                        });

    }

    //TODO: Export

    public void dataToCSV(){

    }

    public void dataToXLS(){

    }

    public void dataToJSON(){

    }

    @Override
    public void setUIStrings() {

    }

}
