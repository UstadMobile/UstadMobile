package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.core.view.SaleListSearchView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.SaleListDetail;

import com.ustadmobile.core.db.dao.SaleDao;

import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;

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

    private long locationUidSelected;

    public SaleListSearchPresenter(Object context, Hashtable arguments,
                                   SaleListSearchView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleDao = repository.getSaleDao();
        locationDao = repository.getLocationDao();

    }

    public long getLocationUidSelected() {
        return locationUidSelected;
    }

    public void setLocationUidSelected(long locationUidSelected) {
        this.locationUidSelected = locationUidSelected;
    }

    public void handleLocationSelected(int selected){
        if(positionToLocation.containsKey(selected)){
            locationUidSelected = positionToLocation.getOrDefault(selected, 0L);
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update location spinner
        locationLiveData = locationDao.findAllActiveLocationsProvider();
        locationLiveData.observe(SaleListSearchPresenter.this,
                SaleListSearchPresenter.this::handleLocationsChanged);

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
}
