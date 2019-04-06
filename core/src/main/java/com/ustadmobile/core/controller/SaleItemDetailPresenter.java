package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.SaleItemDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.lib.db.entities.SaleItem;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;


/**
 * Presenter for SaleItemDetail view
 **/
public class SaleItemDetailPresenter extends UstadBaseController<SaleItemDetailView> {

    UmAppDatabase repository;
    private SaleItemDao saleItemDao;

    private SaleItem currentSaleItem, updatedSaleItem;
    private long productUid, producerUid;

    public SaleItemDetailPresenter(Object context, Hashtable arguments, SaleItemDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        saleItemDao = repository.getSaleItemDao();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(getArguments().containsKey(ARG_SALE_ITEM_PRODUCT_UID)) {
            productUid = Long.parseLong((String) getArguments().get(ARG_SALE_ITEM_PRODUCT_UID));
        }

        if(getArguments().containsKey(ARG_PRODUCER_UID)) {
            producerUid = Long.parseLong((String) getArguments().get(ARG_PRODUCER_UID));
        }

        if(getArguments().containsKey(ARG_SALE_ITEM_UID)){
            initFromSaleItem(Long.parseLong((String) getArguments().get(ARG_SALE_ITEM_UID)));
        }else{

            //Create the new SaleItem
            updatedSaleItem = new SaleItem(productUid);
            saleItemDao.insertAsync(updatedSaleItem, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    initFromSaleItem(result);
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }


    }
    private void initFromSaleItem(long saleItemUid){

        //Observe it.
        UmLiveData<SaleItem> saleItemLiveData = saleItemDao.findByUidLive(saleItemUid);
        saleItemLiveData.observe(SaleItemDetailPresenter.this,
                SaleItemDetailPresenter.this::handleSaleItemChanged);

        //Get the sale item entity
        saleItemDao.findByUidAsync(saleItemUid, new UmCallback<SaleItem>() {
            @Override
            public void onSuccess(SaleItem result) {
                updatedSaleItem = result;
                view.updateSaleItemOnView(updatedSaleItem);
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }

    private void handleSaleItemChanged(SaleItem changedSaleItem){
        if(currentSaleItem == null)
            currentSaleItem = changedSaleItem;

        if(updatedSaleItem == null || !updatedSaleItem.equals(changedSaleItem)) {
            if(changedSaleItem!=null) {
                view.updateSaleItemOnView(updatedSaleItem);
                updatedSaleItem = changedSaleItem;
            }
        }
    }

    public void handleClickSave() {

        if(updatedSaleItem!= null){
            updatedSaleItem.setSaleItemActive(true);

            if(producerUid != 0 && productUid != 0){
                updatedSaleItem.setSaleItemProductUid(productUid);
                updatedSaleItem.setSaleItemProducerUid(producerUid);
            }

            saleItemDao.updateAsync(updatedSaleItem, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    view.finish();
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }
    }

    public void handleChangeQuantity(int quantity){
        updatedSaleItem.setSaleItemQuantity(quantity);

    }

    public void handleChangePPP(long ppp) {
        updatedSaleItem.setSaleItemPricePerPiece(ppp);
    }

    public void updateTotal(int q, long p){
        view.runOnUiThread(() -> view.updateTotal(p*q));
    }

    public void setSold(boolean sold){
        updatedSaleItem.setSaleItemSold(sold);
    }
    public void setPreOrder(boolean po){
        updatedSaleItem.setSaleItemPreorder(po);
    }

}
