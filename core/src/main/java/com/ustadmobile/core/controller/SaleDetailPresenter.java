package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.db.dao.SalePaymentDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SelectProducerView;
import com.ustadmobile.core.view.SelectSaleProductView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItem;

import com.ustadmobile.core.db.dao.SaleItemDao;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;

import jdk.nashorn.internal.runtime.UserAccessorProperty;

import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;

/**
 * Presenter for SaleDetail view
 **/
public class SaleDetailPresenter extends UstadBaseController<SaleDetailView> {

    private UmProvider<SaleItemListDetail> umProvider;
    UmAppDatabase repository;
    private SaleItemDao saleItemDao;
    private SaleDao saleDao;
    private SalePaymentDao salePaymentDao;
    private SaleItem currentSaleItem = null;
    private Sale currentSale;
    private Sale updatedSale;
    private LocationDao locationDao;

    private UmLiveData<List<Location>> locationLiveData;

    private Hashtable<Integer, Long> positionToLocationUid;

    private boolean showSaveButton = false;

    public SaleDetailPresenter(Object context, Hashtable arguments, SaleDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleItemDao = repository.getSaleItemDao();
        salePaymentDao = repository.getSalePaymentDao();
        saleDao = repository.getSaleDao();
        locationDao = repository.getLocationDao();

        positionToLocationUid = new Hashtable<>();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(getArguments().containsKey(ARG_SALE_UID)){
            initFromSale(Long.parseLong((String) getArguments().get(ARG_SALE_UID)));
            showSaveButton = true;
            view.runOnUiThread(() -> {
                view.showCalculations(true);
                view.showDelivered(true);
                view.showNotes(true);
            });

        }else{
            updatedSale = new Sale();
            updatedSale.setSalePreOrder(true); //ie: Not delivered unless ticked.
            updatedSale.setSaleDone(false);
            updatedSale.setSaleActive(false);

            saleDao.insertAsync(updatedSale, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromSale(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }

    }

    private void updateSaleItemProvider(long saleUid){
        //Get provider
        umProvider = saleItemDao.findAllSaleItemListDetailActiveBySaleProvider(saleUid);
        view.setListProvider(umProvider);

    }


    public void getTotalSaleOrderAndDiscountAndUpdateView(long saleUid){
        saleItemDao.getSaleItemCountFromSale(saleUid, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if(result > 0) {
                    view.runOnUiThread(() -> {
                        view.showSaveButton(true);
                        view.showNotes(true);
                        view.showDelivered(true);
                        view.showCalculations(true);
                    });
                }

            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        saleItemDao.findTotalPaidBySaleAsync(saleUid,
                new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {

                view.updateOrderTotal(result);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });


    }

    //Next sprint
    public void getPaymentTotalAndUpdateView(){
        if(currentSaleItem != null){
            salePaymentDao.findTotalPaidBySaleAsync(currentSaleItem.getSaleItemUid(),
                    new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    view.updatePaymentTotal(result);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    public void initFromSale(long saleUid){

        //Observe this sale entity
        UmLiveData<Sale> saleLiveData = saleDao.findByUidLive(saleUid);
        saleLiveData.observe(SaleDetailPresenter.this,
                SaleDetailPresenter.this::handleSaleChanged);

        //Get the sale entity
        saleDao.findByUidAsync(saleUid, new UmCallback<Sale>() {
            @Override
            public void onSuccess(Sale result) {
                updatedSale = result;
                view.updateSaleOnView(updatedSale);

                startObservingLocations();

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        getTotalSaleOrderAndDiscountAndUpdateView(saleUid);
        updateSaleItemProvider(saleUid);
        //Next sprint:
        //getPaymentTotalAndUpdateView();
    }

    public void startObservingLocations(){
        locationLiveData = locationDao.findAllActiveLocationsLive();
        locationLiveData.observe(SaleDetailPresenter.this,
                SaleDetailPresenter.this::handleLocationsChanged);
    }

    public void handleSaleChanged(Sale sale){
        //set the og person value
        if(currentSale == null)
            currentSale = sale;

        if(updatedSale == null || !updatedSale.equals(sale)) {
            if(sale!=null) {
                view.updateSaleOnView(updatedSale);
                updatedSale = sale;
            }
        }

    }

    public void handleLocationsChanged(List<Location> changedLocations){
        int selectedPosition = 0;

        long locationUid = 0;

        if(updatedSale == null){
            updatedSale = new Sale();
        }
        if(updatedSale.getSaleLocationUid() != 0){
            locationUid = updatedSale.getSaleLocationUid();
        }

        positionToLocationUid = new Hashtable<>();

        ArrayList<String> locationList = new ArrayList<>();
        int spinnerId = 0;
        for(Location el : changedLocations){
            positionToLocationUid.put(spinnerId, el.getLocationUid());
            locationList.add(el.getTitle());
            if(locationUid == el.getLocationUid()){
                selectedPosition = spinnerId;
            }
            spinnerId++;
        }
        String[] locationPreset = new String[locationList.size()];
        locationPreset = locationList.toArray(locationPreset);

        view.setLocationPresets(locationPreset, selectedPosition);

    }

    public void handleClickSave() {

        if(updatedSale != null){
            updatedSale.setSaleActive(true);
            saleItemDao.getTitleForSaleUidAsync(updatedSale.getSaleUid(), new UmCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    updatedSale.setSaleTitle(result);
                    saleDao.updateAsync(updatedSale, new UmCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            view.finish();
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });

        }
    }

    public void handleClickSaleItemEdit(long saleItemUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
        impl.go(SaleItemDetailView.VIEW_NAME, args, context);

    }

    public void handleClickAddSaleItem(){


        SaleItem saleItem = new SaleItem();
        saleItem.setSaleItemSaleUid(updatedSale.getSaleUid());
        saleItem.setSaleItemDueDate(UMCalendarUtil.getDateInMilliPlusDays(2));
        saleItemDao.insertAsync(saleItem, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long saleItemUid) {
                saleItem.setSaleItemUid(saleItemUid);

                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                Hashtable<String, String> args = new Hashtable<>();
                args.put(ARG_SALE_ITEM_UID, String.valueOf(saleItemUid));
                impl.go(SelectProducerView.VIEW_NAME, args, context);

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    public void handleDiscountChanged(long discount){
        updatedSale.setSaleDiscount(discount);
        view.updateOrderTotalAfterDiscount(discount);
    }

    public void handleOrderNotesChanged(String notes){
        updatedSale.setSaleNotes(notes);
    }

    public void handleSetDelivered(boolean delivered){
        updatedSale.setSaleDone(delivered);
        updatedSale.setSalePreOrder(!delivered);

    }

    public void handleLocationSelected(int position){
        if(position >= 0 && !positionToLocationUid.isEmpty()
                && positionToLocationUid.containsKey(position)) {
            long locationUid = positionToLocationUid.get(position);
            updatedSale.setSaleLocationUid(locationUid);
        }
    }

    public boolean isShowSaveButton() {
        return showSaveButton;
    }

    public void setShowSaveButton(boolean showSaveButton) {
        this.showSaveButton = showSaveButton;
    }
}
