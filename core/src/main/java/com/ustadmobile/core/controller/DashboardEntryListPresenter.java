package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DashboardTagDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DashboardEntryListView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.lib.db.entities.DashboardEntry;

import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.lib.db.entities.DashboardTag;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.UmAccount;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_DASHBOARD_ENTRY_UID;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_TYPE;
import static com.ustadmobile.lib.db.entities.DashboardEntry.REPORT_TYPE_SALES_LOG;
import static com.ustadmobile.lib.db.entities.DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE;
import static com.ustadmobile.lib.db.entities.DashboardEntry.REPORT_TYPE_TOP_LES;

/**
 * Presenter for DashboardEntryList view
 **/
public class DashboardEntryListPresenter extends UstadBaseController<DashboardEntryListView> {

    private UmProvider<DashboardEntry> entryProvider;
    private UmProvider<DashboardTag> tagProvider;
    UmAppDatabase repository;
    private DashboardEntryDao dashboardEntryDao;
    private DashboardTagDao tagDao;
    private Person loggedInPerson;
    private long loggedInPersonUid = 0L;
    private PersonDao personDao;
    private UmLiveData<List<DashboardTag>> tagLiveData;

    private HashMap<Long, Integer> tagToPosition;
    private HashMap<Integer, Long> positionToTag;

    public DashboardEntryListPresenter(Object context, Hashtable arguments,
                                       DashboardEntryListView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        dashboardEntryDao = repository.getDashboardEntryDao();
        tagDao = repository.getDashboardTagDao();
        personDao = repository.getPersonDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UmAccount activeAccount = UmAccountManager.getActiveAccount(context);

        if(activeAccount != null) {
            loggedInPersonUid = activeAccount.getPersonUid();
            //Get provider
            entryProvider = dashboardEntryDao.findByPersonAndActiveProvider(loggedInPersonUid);
            view.setDashboardEntryProvider(entryProvider);

            tagProvider = tagDao.findAllActiveProvider();
            view.setDashboardTagProvider(tagProvider);

            //Update location spinner
            tagLiveData = tagDao.findAllActiveLive();
            tagLiveData.observe(DashboardEntryListPresenter.this,
                    DashboardEntryListPresenter.this::handleTagsChanged);


        }

    }



    private void handleTagsChanged(List<DashboardTag> tags){

        tagToPosition = new HashMap<>();
        positionToTag = new HashMap<>();

        ArrayList<String> tagList = new ArrayList<>();
        int pos = 0;
        for(DashboardTag el : tags){
            tagList.add(el.getDashboardTagTitle());
            tagToPosition.put(el.getDashboardTagUid(), pos);
            positionToTag.put(pos, el.getDashboardTagUid());
            pos++;
        }
        String[] tagPresets = new String[tagList.size()];
        tagPresets = tagList.toArray(tagPresets);
        view.loadChips(tagPresets);
    }

    public void handleClickSearch(){
        //TODO
    }

    public void handleClickNewSalePerformanceReport(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_TYPE, String.valueOf(REPORT_TYPE_SALES_PERFORMANCE));
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context);

//        //TODO
//        DashboardEntry newEntry = new DashboardEntry("Sales performance Report",
//                REPORT_TYPE_SALES_PERFORMANCE, loggedInPersonUid);
//        dashboardEntryDao.insertAsync(newEntry, new UmCallback<Long>() {
//            @Override
//            public void onSuccess(Long result) {
//                //Do nothing.
//            }
//
//            @Override
//            public void onFailure(Throwable exception) {
//                exception.printStackTrace();
//            }
//        });
    }

    public void handleClickNewSalesLogReport(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_TYPE, String.valueOf(REPORT_TYPE_SALES_LOG));
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context);
//        //TODO
//        DashboardEntry newEntry = new DashboardEntry("Sales log Report",
//                REPORT_TYPE_SALES_LOG, loggedInPersonUid);
//        dashboardEntryDao.insertAsync(newEntry, new UmCallback<Long>() {
//            @Override
//            public void onSuccess(Long result) {
//                //Do nothing.
//            }
//
//            @Override
//            public void onFailure(Throwable exception) {
//                exception.printStackTrace();
//            }
//        });
    }

    public void handleClickTopLEsReport(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_TYPE, String.valueOf(REPORT_TYPE_TOP_LES));
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context);
//        //TODO
//        DashboardEntry newEntry = new DashboardEntry("Top LEs Report",
//                REPORT_TYPE_TOP_LES, loggedInPersonUid);
//        dashboardEntryDao.insertAsync(newEntry, new UmCallback<Long>() {
//            @Override
//            public void onSuccess(Long result) {
//                //Do nothing.
//            }
//
//            @Override
//            public void onFailure(Throwable exception) {
//                exception.printStackTrace();
//            }
//        });
    }

    /**
     * Primary action on item.
     */
    public void handleAddTag(long entryUid, long tagUid){

    }

    /**
     * Secondary action on item.
     */
    public void handleSetTitle(long entryUid, String title){

        dashboardEntryDao.updateTitle(entryUid, title, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                //Do nothing..
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void handleDeleteEntry(long entryUid){
        dashboardEntryDao.deleteEntry(entryUid, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                //Do nothing..
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void handleEditEntry(long entryUid){
        //Go to Report Options with the data here.
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_DASHBOARD_ENTRY_UID, String.valueOf(entryUid));
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context);
    }

    public void handleChangeTitle(long entryUid, String existingTitle){

        view.showSetTitle(existingTitle, entryUid);
    }

    public void handlePinEntry(long entryUid, boolean pinned){
        if(pinned){
            dashboardEntryDao.unpinEntry(entryUid, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    //Do nothing..
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else {
            dashboardEntryDao.pinEntry(entryUid, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    //Do nothing..
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

}
