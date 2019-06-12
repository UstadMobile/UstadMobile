package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.core.view.SaleListSearchView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SelectDateRangeDialogView;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.SaleListDetail;

import com.ustadmobile.core.db.dao.SaleDao;

import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;
import static com.ustadmobile.core.view.SaleListSearchView.SORT_HIGHEST_PRICE;
import static com.ustadmobile.core.view.SaleListSearchView.SORT_LOWEST_PRICE;
import static com.ustadmobile.core.view.SaleListSearchView.SORT_MOST_RECENT;
import static com.ustadmobile.core.view.SelectDateRangeDialogView.ARG_FROM_DATE;
import static com.ustadmobile.core.view.SelectDateRangeDialogView.ARG_TO_DATE;

/**
 * Presenter for SaleListSearch view
 **/
public class SaleListSearchPresenter extends CommonHandlerPresenter<SaleListSearchView> {

    private UmProvider<SaleListDetail> umProvider;
    private UmLiveData<List<Location>> locationLiveData;
    UmAppDatabase repository;
    private SaleDao saleDao;
    private LocationDao locationDao;

    private HashMap<Long, Integer> locationToPosition;
    private HashMap<Integer, Long> positionToLocation;

    private Hashtable<Long, Integer> idToOrderInteger;

    private long from,to;

    private long locationUidSelected;

    public SaleListSearchPresenter(Object context, Hashtable arguments,
                                   SaleListSearchView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleDao = repository.getSaleDao();
        locationDao = repository.getLocationDao();

    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private void updateSortSpinnerPreset(){
        ArrayList<String> presetAL = new ArrayList<>();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        idToOrderInteger = new Hashtable<>();

        presetAL.add(impl.getString(MessageID.most_recent, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_MOST_RECENT);

        presetAL.add(impl.getString(MessageID.lowest_price, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_LOWEST_PRICE);

        presetAL.add(impl.getString(MessageID.highest_price, getContext()));
        idToOrderInteger.put((long) presetAL.size(), SORT_HIGHEST_PRICE);

        String[] sortPresets = arrayListToStringArray(presetAL);

        view.updateSortSpinner(sortPresets);
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

    public long getLocationUidSelected() {
        return locationUidSelected;
    }

    public void setLocationUidSelected(long locationUidSelected) {
        this.locationUidSelected = locationUidSelected;
    }

    public void handleLocationSelected(int selected){
        if(positionToLocation.containsKey(selected)){

            if(positionToLocation.containsKey(selected)){
                locationUidSelected = positionToLocation.get(selected);
            }else{
                locationUidSelected = 0L;
            }
            //TODO: Update filter and set provider.

            setProvider();
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update location spinner
        locationLiveData = locationDao.findAllActiveLocationsProvider();
        locationLiveData.observe(SaleListSearchPresenter.this,
                SaleListSearchPresenter.this::handleLocationsChanged);

        idToOrderInteger = new Hashtable<>();

        updateSortSpinnerPreset();

        //Get provider
        umProvider = saleDao.findAllSaleFilterAndSearchProvider(
                0,0,1,"%");
        setProvider();
    }

    private void handleLocationsChanged(List<Location> locations){

        locationToPosition = new HashMap<>();
        positionToLocation = new HashMap<>();

        ArrayList<String> locationList = new ArrayList<>();
        int pos = 0;
        for(Location el : locations){
            locationList.add(el.getTitle());
            locationToPosition.put(el.getLocationUid(), pos);
            positionToLocation.put(pos, el.getLocationUid());
            pos++;
        }
        String[] locationPresets = new String[locationList.size()];
        locationPresets = locationList.toArray(locationPresets);
        view.updateLocationSpinner(locationPresets)
        ;
    }

    public void updateFilter(float apl,float aph, String value){
        String stringQuery = "%" + value + "%";
        umProvider = saleDao.findAllSaleFilterAndSearchProvider(locationUidSelected,apl,aph,
                stringQuery);
        setProvider();
    }

    /**
     * Sets the people list provider set in the Presenter to the View.
     */
    private void setProvider(){
        view.setListProvider(umProvider);
    }

    public void goToSelectDateRange(long from, long to){
        UstadMobileSystemImpl impl =UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        if(from >0 && to > 0) {
            args.put(ARG_FROM_DATE, String.valueOf(from));
            args.put(ARG_TO_DATE, String.valueOf(to));
        }
        impl.go(SelectDateRangeDialogView.VIEW_NAME, args, context);
    }


    private void handleClickSale(long saleUid){
        UstadMobileSystemImpl impl =UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_UID, String.valueOf(saleUid));
        impl.go(SaleDetailView.VIEW_NAME, args, context);

    }

    @Override
    public void handleCommonPressed(Object arg) {
        handleClickSale((Long)arg);
    }

    @Override
    public void handleSecondaryPressed(Object arg) {}

    public void handleDateSelected(long fromDate, long toDate, String dateRangeText) {
        //Update Date range text.
        //TODO: Update from, to dates and update query and provider
        from = fromDate;
        to = toDate;

        //Update filter and setprovider
        setProvider();

        view.updateDateRangeText(dateRangeText);

    }

    public void handleChangeSortOrder(long order) {

        order=order+1;
        if(idToOrderInteger.containsKey(order)){
            int sortCode = idToOrderInteger.get(order);
            //TODO: Update provider
            setProvider();
        }
    }
}
