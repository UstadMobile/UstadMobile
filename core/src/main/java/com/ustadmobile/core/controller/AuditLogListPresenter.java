package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.AuditLogDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AuditLogListView;
import com.ustadmobile.lib.db.entities.AuditLog;
import com.ustadmobile.lib.db.entities.AuditLogWithNames;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Person;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.controller.ReportOverallAttendancePresenter.convertLongArray;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_ACTOR_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_CLASS_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_FROM_TIME;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_LOCATION_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_PEOPLE_LIST;
import static com.ustadmobile.core.view.AuditLogSelectionView.ARG_AUDITLOG_TO_TIME;

/**
 * Presenter for AuditLogList view
 **/
public class AuditLogListPresenter extends UstadBaseController<AuditLogListView> {

    private UmProvider<AuditLogWithNames> umProvider;
    UmAppDatabase repository;
    private AuditLogDao providerDao;
    private long fromDate;
    private long toDate;
    private List<Long> locationList;
    private List<Long> clazzesList;
    private List<Long> peopleList;
    private List<Long> actorList;


    public AuditLogListPresenter(Object context, Hashtable arguments, AuditLogListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        locationList = new ArrayList<>();
        clazzesList = new ArrayList<>();
        peopleList = new ArrayList<>();
        actorList = new ArrayList<>();

        //Get provider Dao
        providerDao = repository.getAuditLogDao();

        if(arguments.containsKey(ARG_AUDITLOG_FROM_TIME)){
            fromDate = (long) arguments.get(ARG_AUDITLOG_FROM_TIME);
        }
        if(arguments.containsKey(ARG_AUDITLOG_TO_TIME)){
            toDate = (long) arguments.get(ARG_AUDITLOG_TO_TIME);
        }

        if(arguments.containsKey(ARG_AUDITLOG_LOCATION_LIST)){
            long[] locations = (long[]) arguments.get(ARG_AUDITLOG_LOCATION_LIST);
            locationList = convertLongArray(locations);
        }
        if(arguments.containsKey(ARG_AUDITLOG_CLASS_LIST)){
            long[] clazzes = (long[]) arguments.get(ARG_AUDITLOG_CLASS_LIST);
            clazzesList = convertLongArray(clazzes);
        }

        if(arguments.containsKey(ARG_AUDITLOG_PEOPLE_LIST)){
            long[] people = (long[]) arguments.get(ARG_AUDITLOG_PEOPLE_LIST);
            peopleList = convertLongArray(people);
        }

        if(arguments.containsKey(ARG_AUDITLOG_ACTOR_LIST)){
            long[] actors = (long[]) arguments.get(ARG_AUDITLOG_ACTOR_LIST);
            actorList = convertLongArray(actors);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = providerDao.findAllAuditLogsWithNameFilter(fromDate, toDate, locationList,
                clazzesList, peopleList, actorList);
        view.setListProvider(umProvider);

    }

    public void dataToCSV(){

        //Get all as list
        List<String[]> data = new ArrayList<>();

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        String changedString= impl.getString(MessageID.changed,context);
        String clazzType = impl.getString(MessageID.clazz,context);
        String personType = impl.getString(MessageID.person,context);

        providerDao.findAllAuditLogsWithNameFilterList(fromDate, toDate, locationList,
            clazzesList, peopleList, actorList, new UmCallback<List<AuditLogWithNames>>() {
            @Override
            public void onSuccess(List<AuditLogWithNames> result) {
                for(AuditLogWithNames entity : result){
                    //"Actor changed Entity Type Entity Name at Time"
                    String entityType = "";
                    String entityName = "";
                    switch(entity.getAuditLogTableUid()){
                        case Clazz.TABLE_ID:
                            entityType = clazzType;
                            entityName = entity.getClazzName();
                            break;
                        case Person.TABLE_ID:
                            entityType = personType;
                            entityName = entity.getPersonName();
                            break;
                        default:
                            break;
                    }
                    String logString = entity.getActorName() + " " + changedString + " " +
                            entityType + " " + entityName;
                    String[] a ={logString};
                    data.add(a);

                }
                view.generateCSVReport(data);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });



    }

}
