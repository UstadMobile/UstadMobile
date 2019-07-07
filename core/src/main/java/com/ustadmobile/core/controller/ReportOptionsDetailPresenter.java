package com.ustadmobile.core.controller;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DashboardEntryDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ReportOptions;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.core.view.ReportSalesLogDetailView;
import com.ustadmobile.core.view.ReportSalesPerformanceDetailView;
import com.ustadmobile.core.view.ReportTopLEsDetailView;
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView;
import com.ustadmobile.core.view.SelectMultiplePeopleView;
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView;
import com.ustadmobile.lib.db.entities.DashboardEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_DASHBOARD_ENTRY_UID;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;
import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_TYPE;
import static com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.ARG_LOCATIONS_SET;
import static com.ustadmobile.core.view.SelectMultiplePeopleView.ARG_SELECTED_PEOPLE;
import static com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView.ARG_PRODUCT_SELECTED_SET;

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

    private long fromDate, toDate;
    private int fromPrice, toPrice;

    private List<Long> selectedLocations;
    private List<Long> selectedProducts;
    private List<Long> selectedLEs;

    private ReportOptions reportOptions;
    private String reportOptionString;

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

        if(getArguments().containsKey(ARG_REPORT_OPTIONS)){
            reportOptionString = getArguments().get(ARG_REPORT_OPTIONS).toString();
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

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
        }else{
            reportOptions = new ReportOptions();
            //Set title based on given.
            setTitleFromArgs();
            initFromDashboardEntry();

        }
    }

    private void setReportOptionFromString(String reportOptionsString){
        Gson gson = new Gson();
        reportOptions = gson.fromJson(reportOptionsString, ReportOptions.class);
        fromDate = reportOptions.getFromDate();
        toDate = reportOptions.getToDate();
        fromPrice = reportOptions.getFromPrice();
        toPrice = reportOptions.getToPrice();
    }

    private void initFromDashboardEntry(){
        if(currentDashboardEntry!= null) {
            //Populate filter from entity.
            String dashboardReportOptionsString = currentDashboardEntry.getDashboardEntryReportParam();

            if (dashboardReportOptionsString != null && !dashboardReportOptionsString.isEmpty()) {
                setReportOptionFromString(dashboardReportOptionsString);
            }
        }else if (reportOptionString != null && !reportOptionString.isEmpty()){
            setReportOptionFromString(reportOptionString);
        }

        view.setEditMode(currentDashboardEntry != null);

        //Build report options on view:

        selectedLocations = reportOptions.getLocations();
        selectedLEs = reportOptions.getLes();
        selectedProducts = reportOptions.getProductTypes();
        currentGroupBy = reportOptions.getGroupBy();

        //Date range
        updateDateRangeOnView();

        //Sale price rage
        updateSalePriceRangeOnView();

        //Show Average
        view.setShowAverage(reportOptions.isShowAverage());


        if(selectedLocations.isEmpty()){
            view.setLocationSelected(impl.getString(MessageID.all, context));
        }else{
            LocationDao locationDao = repository.getLocationDao();
            locationDao.findAllLocationNamesInUidList(selectedLocations, new UmCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    view.setLocationSelected(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }

        if(selectedProducts.isEmpty()){
            view.setProductTypeSelected(impl.getString(MessageID.all, context));
        }else{
            SaleProductDao saleProductDao = repository.getSaleProductDao();
            saleProductDao.findAllProductNamesInUidList(selectedProducts, new UmCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    view.setProductTypeSelected(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }

        if(selectedLEs.isEmpty()){
            view.setLESelected(impl.getString(MessageID.all, context));
        }else{
            PersonDao personDao = repository.getPersonDao();
            personDao.findAllPeopleNamesInUidList(selectedLEs, new UmCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    view.setLESelected(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }

        //Group by
        populateGroupBy();

    }

    public void updateSalePriceRangeOnView(){

        DecimalFormat formatter = new DecimalFormat("#,###");
        String toS = formatter.format(toPrice);
        String fromS = formatter.format(fromPrice);

        String rangeText = impl.getString(MessageID.from, context) + " "
                + fromS + " Afs - " + toS + " Afs";
        view.setSalePriceRangeSelected(fromPrice, toPrice, rangeText);


    }
    public void updateDateRangeOnView(){
        Locale currentLocale = Locale.getDefault();

        if(fromDate == 0 && toDate == 0){
            fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31);
            toDate = UMCalendarUtil.getDateInMilliPlusDays(0);
        }

        String dateRangeText = UMCalendarUtil.getPrettyDateSimpleFromLong(fromDate,
                currentLocale) + " - " + UMCalendarUtil.getPrettyDateSimpleFromLong(toDate,
                currentLocale);

        view.setDateRangeSelected(dateRangeText);

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

        view.setGroupByPresets(sortPresets, currentGroupBy-1);
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

        //Add remainder bits
        reportOptions.setFromPrice(fromPrice);
        reportOptions.setToPrice(toPrice);
        reportOptions.setFromDate(fromDate);
        reportOptions.setToDate(toDate);
        reportOptions.setLocations(selectedLocations);
        reportOptions.setProductTypes(selectedProducts);
        reportOptions.setLes(selectedLEs);

        //Create json from reportOptions
        Gson gson = new Gson();
        String reportOptionsString = gson.toJson(reportOptions);

        //Update dashboard entry
        if(currentDashboardEntry != null){
            currentDashboardEntry.setDashboardEntryReportParam(reportOptionsString);
            dashboardEntryDao.updateAsync(currentDashboardEntry, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    view.finish();
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else {

            Hashtable<String, String> args = new Hashtable<>();
            args.put(ARG_REPORT_TYPE, String.valueOf(reportType));
            args.put(ARG_REPORT_OPTIONS, reportOptionsString);

            switch (reportType) {
                case DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE:
                    impl.go(ReportSalesPerformanceDetailView.VIEW_NAME, args, context);
                    break;
                case DashboardEntry.REPORT_TYPE_SALES_LOG:
                    impl.go(ReportSalesLogDetailView.VIEW_NAME, args, context);
                    break;
                case DashboardEntry.REPORT_TYPE_TOP_LES:
                    impl.go(ReportTopLEsDetailView.VIEW_NAME, args, context);
                    break;
                default:
                    break;
            }
            view.finish();
        }
    }

    public void goToProductSelect(){
        Hashtable<String, String> args = new Hashtable<>();

        if(selectedProducts != null && !selectedProducts.isEmpty()){
            String selectedProductTypesCSString = convertLongListToStringCSV(selectedProducts);
            args.put(ARG_PRODUCT_SELECTED_SET, selectedProductTypesCSString);
        }

        impl.go(SelectMultipleProductTypeTreeDialogView.VIEW_NAME, args, context);
    }

    public void goToLEsSelect(){
        Hashtable<String, String> args = new Hashtable<>();
        if(selectedLEs != null && !selectedLEs.isEmpty()){
            String selectedLEsCSSting = convertLongListToStringCSV(selectedLEs);
            args.put(ARG_SELECTED_PEOPLE, selectedLEsCSSting);
        }
        impl.go(SelectMultiplePeopleView.VIEW_NAME, args, context);
    }

    public void goToLocationSelect(){
        Hashtable<String, String> args = new Hashtable<>();

        if(selectedLocations != null && !selectedLocations.isEmpty()){
            String selectedLocationsCSString = convertLongListToStringCSV(selectedLocations);
            args.put(ARG_LOCATIONS_SET, selectedLocationsCSString);
        }

        impl.go(SelectMultipleLocationTreeDialogView.VIEW_NAME, args, context);
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

    public void handleToggleAverage(boolean ticked){
        reportOptions.setShowAverage(ticked);
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public void setFromPrice(int fromPrice) {
        this.fromPrice = fromPrice;
    }

    public void setToPrice(int toPrice) {
        this.toPrice = toPrice;
    }

    /**
     * Convert LongList to CSV String
     * @param longList    Long list
     */
    public static String convertLongListToStringCSV(List<Long> longList){
        return longList.toString().replaceAll("\\[|\\]", "");
    }

    public static List<Long> convertCSVStringToLongList(String csString){
        List<Long> list = new ArrayList<Long>();
        for (String s : csString.split(","))
            list.add(Long.parseLong(s.trim()));
        return list;
    }
}
