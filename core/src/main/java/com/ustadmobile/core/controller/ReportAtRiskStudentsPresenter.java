package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.CallPersonRelatedDialogView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.ReportAtRiskStudentsView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ustadmobile.core.controller.ReportOverallAttendancePresenter.convertLongArray;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.ReportEditView.ARG_CLAZZ_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_GENDER_DISAGGREGATE;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATION_LIST;
import static com.ustadmobile.core.view.ReportEditView.ARG_TO_DATE;

public class ReportAtRiskStudentsPresenter extends CommonHandlerPresenter<ReportAtRiskStudentsView> {

    private List<Long> clazzList, locationList;
    private boolean genderDisaggregated = true;
    public static final float RISK_THRESHOLD = 0.4f;

    private UmProvider<PersonWithEnrollment> atRiskStudentsUmProvider;

    private LinkedHashMap<String, List<PersonWithEnrollment>> dataMapsMap;

    UmAppDatabase repository;

    public ReportAtRiskStudentsPresenter(Object context, Hashtable arguments,
                                         ReportAtRiskStudentsView view){
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        dataMapsMap = new LinkedHashMap<>();
        clazzList = new ArrayList<>();
        locationList = new ArrayList<>();
        if(arguments.containsKey(ARG_FROM_DATE)){
            long fromDate = (long) arguments.get(ARG_FROM_DATE);
        }
        if(arguments.containsKey(ARG_TO_DATE)){
            long toDate = (long) arguments.get(ARG_TO_DATE);
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
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
        getDataAndUpdateView();
    }

    public void dataToCSV(){
        view.generateCSVReport();
    }

    /**
     * Queries database, gets raw report data, updates view. The Guts of the logic
     */
    private void getDataAndUpdateView(){

        ClazzDao clazzDao = repository.getClazzDao();
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();

        //1. Build a list of all classes for this report. This comes from both the location
        // and the specified clazz list.
        clazzDao.findAllClazzesByLocationAndUidList(locationList, clazzList,
                new UmCallback<List<Clazz>>() {
            @Override
            public void onSuccess(List<Clazz> clazzList) {

                //build a long list of classes.
                List<Long> clazzUidList = new ArrayList<>();
                for(Clazz everyClazz:clazzList){
                    clazzUidList.add(everyClazz.getClazzUid());
                }

                //Run Live Data Query
                atRiskStudentsUmProvider =
                        clazzMemberDao.findAllStudentsAtRiskForClazzListAsync(clazzUidList,
                                RISK_THRESHOLD);
                updateProviderToView();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Sets provider to the view
     */
    private void updateProviderToView(){
        view.setReportProvider(atRiskStudentsUmProvider);
    }

    public boolean isGenderDisaggregated() {
        return genderDisaggregated;
    }

    public void setGenderDisaggregated(boolean genderDisaggregated) {
        this.genderDisaggregated = genderDisaggregated;
    }

    @Override
    public void handleCommonPressed(Object arg) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_PERSON_UID, (long)arg);
        impl.go(PersonDetailView.VIEW_NAME, args, view.getContext());

    }

    @Override
    public void handleSecondaryPressed(Object arg) {
        PersonWithEnrollment personWithEnrollment = (PersonWithEnrollment)arg;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_PERSON_UID, personWithEnrollment.getPersonUid());
        args.put(ARG_CLAZZ_UID, personWithEnrollment.getClazzUid());
        impl.go(CallPersonRelatedDialogView.VIEW_NAME, args, view.getContext());
    }
}
