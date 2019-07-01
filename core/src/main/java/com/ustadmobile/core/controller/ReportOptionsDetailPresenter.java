package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.lib.db.entities.DashboardEntry;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_DASHBOARD_ENTRY_UID;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_TYPE;
import static com.ustadmobile.core.view.SelectMultipleTreeDialogView.ARG_LOCATIONS_SET;

/**
 * Presenter for ReportOptionsDetail view
 **/
public class ReportOptionsDetailPresenter extends UstadBaseController<ReportOptionsDetailView> {

    UmAppDatabase repository;
    private DashboardEntryDao dashboardEntryDao;

    private Hashtable<Long, Integer> idToGroupByInteger;
    public static final int GROUP_BY_LOCATION = 1;
    public static final int GROUP_BY_PRODUCT_TYPE = 2;
    public static final int GROUP_BY_GRANTEE = 3;

    private int currentGroupBy = 0;

    private int reportType = 0;
    UstadMobileSystemImpl impl;
    private DashboardEntry currentDashboardEntry = null;
    private long dashboardEntryUid = 0L;

    private List<Long> selectedLocations;
    private List<Long> selectedProducts;
    private List<Long> selectedLEs;

    public ReportOptionsDetailPresenter(Object context, Hashtable arguments,
                                        ReportOptionsDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        dashboardEntryDao = repository.getDashboardEntryDao();

        impl = UstadMobileSystemImpl.getInstance();

        if(getArguments().containsKey(ARG_REPORT_TYPE)){
            reportType = Integer.valueOf(getArguments().get(ARG_REPORT_TYPE).toString());
        }

        if(getArguments().containsKey(ARG_DASHBOARD_ENTRY_UID)){
            dashboardEntryUid =
                    Long.parseLong(getArguments().get(ARG_DASHBOARD_ENTRY_UID).toString());
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate group by
        populateGroupBy();

        if(dashboardEntryUid != 0){
            dashboardEntryDao.findByUidAsync(dashboardEntryUid, new UmCallback<DashboardEntry>() {
                @Override
                public void onSuccess(DashboardEntry result) {
                    if(result!= null) {
                        view.setTitle(result.getDashboardEntryTitle());
                        currentDashboardEntry = result;
                        initFromDashboardEntry();
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else {
            //Set title based on given.
            setTitleFromArgs();
        }
    }


    private void initFromDashboardEntry(){
        if(currentDashboardEntry!= null){
            //Populate filter from entity.
            String reportOptions = currentDashboardEntry.getDashboardEntryReportParam();
            //TODO
            //Step 1: convert options to json
            //2: loop over fields.
            //3. Build the param for the where clause (save it to this persenter)
            //4. Update view with options

        }
    }

    private void setTitleFromArgs(){
        String reportTitle = impl.getString(MessageID.report_options, context);
        switch(reportType){
            case DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE:
                reportTitle = impl.getString(MessageID.sales_performance_report_options, context);
                break;
            case DashboardEntry.REPORT_TYPE_SALES_LOG:
                reportTitle = impl.getString(MessageID.sales_log_report_options, context);
                break;
            case DashboardEntry.REPORT_TYPE_TOP_LES:
                reportTitle = impl.getString(MessageID.top_les_report_options, context);
                break;
            default:
                break;
        }

        view.setTitle(reportTitle);
    }
    private void populateGroupBy(){
        ArrayList<String> presetAL = new ArrayList<>();

        idToGroupByInteger = new Hashtable<>();

        presetAL.add(impl.getString(MessageID.location, getContext()));
        idToGroupByInteger.put((long) presetAL.size(), GROUP_BY_LOCATION);
        presetAL.add(impl.getString(MessageID.product_type, getContext()));
        idToGroupByInteger.put((long) presetAL.size(), GROUP_BY_PRODUCT_TYPE);
        presetAL.add(impl.getString(MessageID.grantee, getContext()));
        idToGroupByInteger.put((long) presetAL.size(), GROUP_BY_GRANTEE);


        String[] sortPresets = arrayListToStringArray(presetAL);

        //TODO: Modify to send set position from options
        view.setGroupByPresets(sortPresets);
    }

    public void handleChangeGroupBy(long order){
        order=order+1;

        if(idToGroupByInteger.containsKey(order)){
            currentGroupBy= idToGroupByInteger.get(order);
        }
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private String[] arrayListToStringArray(ArrayList<String> presetAL){
        Object[] objectArr = presetAL.toArray();
        String[] strArr = new String[objectArr.length];
        for(int j = 0 ; j < objectArr.length ; j ++){
            strArr[j] = (String) objectArr[j];
        }
        return strArr;
    }
    public void handleClickCreateReport(){
        //TODO
    }

    public void goToProductSelect(){
        //TODO : Open multi tree select
    }

    public void goToLEsSelect(){
        //TODO : Open multi tree select
    }

    public void goToLocationSelect(){
        //TODO: Open multi tree select
        Hashtable<String, Object> args = new Hashtable<>();

        if(selectedLocations != null && !selectedLocations.isEmpty()){
            Long[] selectedLocationsArray =
                    convertLongList(selectedLocations);
            args.put(ARG_LOCATIONS_SET, selectedLocationsArray);
        }

        impl.go(SelectMultipleTreeDialogView.VIEW_NAME, args, context);
    }

    private static Long[] convertLongList(List<Long> list){
        Long[] array = new Long[list.size()];
        int i=0;
        for(Long everyList:list){
            array[i] = everyList;
            i++;
        }
        return array;
    }

    public void handleToggleAverage(boolean ticked){
        //TODO: Save, and process further, etc.
    }

    public void setSelectedLocations(List<Long> selectedLocations) {
        this.selectedLocations = selectedLocations;
    }

    public void setSelectedProducts(List<Long> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    public void setSelectedLEs(List<Long> selectedLEs) {
        this.selectedLEs = selectedLEs;
    }
}
