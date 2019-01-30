package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionResponseNominationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ReportSELView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.SELNominationItem;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponse;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        //TODOing: This


        ClazzDao clazzDao = repository.getClazzDao();
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();

        HashMap<String, List<ClazzMemberWithPerson>> clazzToStudents = new HashMap<>();

        LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap =
                new LinkedHashMap<>();

        SocialNominationQuestionResponseNominationDao nominationDao =
                repository.getSocialNominationQuestionResponseNominationDao();
        nominationDao.getAllNominationsReport(new UmCallback<List<SELNominationItem>>() {
            @Override
            public void onSuccess(List<SELNominationItem> allNominations) {

                //TODO: Handle QuestionSet grouping ?
                int size = allNominations.size();
                System.out.println(size);
                int index = 0;
                for(SELNominationItem everyNomination: allNominations){
                    index++;
                    long thisClazzUid = everyNomination.getClazzUid();
                    String thisClazzName = everyNomination.getClazzName();
                    String thisQuestionTitle = everyNomination.getQuestionText();
                    long nomineeUid = everyNomination.getNomineeUid();
                    long nominatorUid = everyNomination.getNominatorUid();

                    LinkedHashMap<String, Map<Long, List<Long>>> questionMap;
                    Map<Long, List<Long>> nominationMap;
                    List<Long> nominations;

                    if(!clazzMap.containsKey(thisClazzName)){
                        questionMap = new LinkedHashMap<>();
                        nominationMap = new HashMap<>();
                        nominations = new ArrayList<>();

                    }else{
                        questionMap = clazzMap.get(thisClazzName);

                        if(!questionMap.containsKey(thisQuestionTitle)){
                            nominationMap = new HashMap<>();
                            nominations = new ArrayList<>();
                        }else{
                            nominationMap = questionMap.get(thisQuestionTitle);
                            if(!nominationMap.containsKey(nominatorUid)){
                                nominations = new ArrayList<>();
                            }else{
                                nominations = nominationMap.get(nominatorUid);
                            }
                        }
                    }

                    nominations.add(nomineeUid);
                    nominationMap.put(nominatorUid, nominations);
                    questionMap.put(thisQuestionTitle, nominationMap);

                    clazzMap.put(thisClazzName, questionMap);

                    //Build students map
                    if(!clazzToStudents.containsKey(thisClazzName)) {

                        int finalIndex = index;
                        clazzMemberDao.findClazzMemberWithPersonByRoleForClazzUid(thisClazzUid,
                            ClazzMember.ROLE_STUDENT, new UmCallback<List<ClazzMemberWithPerson>>() {
                                @Override
                                public void onSuccess(List<ClazzMemberWithPerson> result) {
                                    clazzToStudents.put(thisClazzName, result);
                                    if(finalIndex >= allNominations.size()){
                                        sendToView(clazzMap, clazzToStudents);
                                    }

                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });
                    }else{
                        if(index >= allNominations.size()){
                            sendToView(clazzMap, clazzToStudents);
                        }
                    }
                }




            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    private void sendToView(LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap,
                            HashMap<String, List<ClazzMemberWithPerson>> clazzToStudents){

        view.runOnUiThread(() -> view.updateTables(clazzMap, clazzToStudents));

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
