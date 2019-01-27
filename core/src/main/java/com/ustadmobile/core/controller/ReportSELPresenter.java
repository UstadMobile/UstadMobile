package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.ReportSELView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.controller.ReportOverallAttendancePresenter.convertLongArray;
import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;

public class ReportSELPresenter extends UstadBaseController<ReportSELView> {

    private long fromDate, toDate;
    private List<Long> clazzList, locationList;
    private boolean genderDisaggregated = false;

    private List dataMap;

    UmAppDatabase repository;


    public ReportSELPresenter(Object context, Hashtable arguments, ReportSELView view) {
        super(context, arguments, view);
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        dataMap = new ArrayList<>();
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

        if(arguments.containsKey(ARG_GENDER_DISAGGREGATE)){
            genderDisaggregated = (Boolean) arguments.get(ARG_GENDER_DISAGGREGATE);
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
    private void getDataAndUpdateTable() {

        long currentTime = System.currentTimeMillis();

        //TODO: This

        //when done:
        //view.updateTables(dataMap);



    }
    public boolean isGenderDisaggregated() {
        return genderDisaggregated;
    }

    public void setGenderDisaggregated(boolean genderDisaggregated) {
        this.genderDisaggregated = genderDisaggregated;
    }

    @Override
    public void setUIStrings() {

    }
}
