package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;


/**
 * The ReportNumberOfDaysClassesOpen Presenter.
 */
public class ReportNumberOfDaysClassesOpenPresenter
        extends UstadBaseController<ReportNumberOfDaysClassesOpenView> {

    private long fromDate;
    private long toDate;
    private long[] locations;
    private long[] clazzes;
    private List<Long> clazzList;
    private List<Long> locationList;
    public List<Long> barChartTimestamps;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public static ArrayList<Long> convertLongArray(long[] array) {
        ArrayList<Long> result = new ArrayList<Long>(array.length);
        for (long item : array)
            result.add(item);
        return result;
    }

    public ReportNumberOfDaysClassesOpenPresenter(Object context, Hashtable arguments,
                                                  ReportNumberOfDaysClassesOpenView view) {
        super(context, arguments, view);

        clazzList = new ArrayList<>();
        locationList = new ArrayList<>();

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
            clazzList = convertLongArray(clazzes);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        getNumberOfDaysOpenDataAndUpdateCharts();
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private void getNumberOfDaysOpenDataAndUpdateCharts(){

        LinkedHashMap<Float, Float> dataMap = new LinkedHashMap<>();

        //TODO: Account for location and clazzes.

        repository.getClazzLogDao().getNumberOfClassesOpenForDateLocationClazzes(fromDate, toDate,
                clazzList, locationList,
                new UmCallback<List<ClazzLogDao.NumberOfDaysClazzesOpen>>() {
            @Override
            public void onSuccess(List<ClazzLogDao.NumberOfDaysClazzesOpen> resultList) {
                for(ClazzLogDao.NumberOfDaysClazzesOpen everyResult:resultList){
                    dataMap.put(
                            everyResult.getDate() / 1000f,
                            (float) everyResult.getNumber());
                }
                //Update the report data on the view:
                view.updateBarChart(dataMap);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
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
