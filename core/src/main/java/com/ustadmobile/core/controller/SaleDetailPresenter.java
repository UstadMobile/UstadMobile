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



    public SaleDetailPresenter(Object context, Hashtable arguments, SaleDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleItemDao = repository.getSaleItemDao();
        salePaymentDao = repository.getSalePaymentDao();
        saleDao = repository.getSaleDao();
        locationDao = repository.getLocationDao();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get provider 
        umProvider = saleItemDao.findAllSaleItemListDetailActiveProvider();
        view.setListProvider(umProvider);

        if(getArguments().containsKey(ARG_SALE_UID)){
            initFromSale(Long.parseLong((String) getArguments().get(ARG_SALE_UID)));
        }else{
            updatedSale = new Sale();
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

        ArrayList<String> locationList = new ArrayList<>();
        int spinnerId = 0;
        for(Location el : changedLocations){
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
            saleDao.updateAsync(updatedSale, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    view.finish();
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
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
    }
}
