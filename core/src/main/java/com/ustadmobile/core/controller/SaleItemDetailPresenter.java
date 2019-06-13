package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleItemDao;
import com.ustadmobile.core.db.dao.SaleItemReminderDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.view.AddReminderDialogView;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleItemReminder;
import com.ustadmobile.lib.db.entities.SaleProduct;

import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_PRODUCT_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SelectProducerView.ARG_PRODUCER_UID;


/**
 * Presenter for SaleItemDetail view
 **/
public class SaleItemDetailPresenter extends UstadBaseController<SaleItemDetailView> {

    UmAppDatabase repository;
    private SaleItemDao saleItemDao;
    private SaleItemReminderDao reminderDao;

    private SaleItem currentSaleItem, updatedSaleItem;
    private long productUid, producerUid, saleItemUid;

    private boolean refreshSaleItem = true;

    public SaleItemDetailPresenter(Object context, Hashtable arguments, SaleItemDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        saleItemDao = repository.getSaleItemDao();
        reminderDao = repository.getSaleItemReminderDao();
    }

    public SaleItemDetailPresenter(Object context, Hashtable arguments, SaleItemDetailView view,
                                   boolean refresh) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        saleItemDao = repository.getSaleItemDao();
        reminderDao = repository.getSaleItemReminderDao();
        refreshSaleItem = refresh;
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

        if(getArguments().containsKey(ARG_SALE_ITEM_UID)) {
            saleItemUid = Long.parseLong(getArguments().get(ARG_SALE_ITEM_UID).toString());
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

        if(refreshSaleItem) {
            //Observe it.
            UmLiveData<SaleItem> saleItemLiveData = saleItemDao.findByUidLive(saleItemUid);
            saleItemLiveData.observe(SaleItemDetailPresenter.this,
                    SaleItemDetailPresenter.this::handleSaleItemChanged);


            //Get the sale item entity
            saleItemDao.findByUidAsync(saleItemUid, new UmCallback<SaleItem>() {
                @Override
                public void onSuccess(SaleItem result) {
                    updatedSaleItem = result;
                    long saleProductUid = 0;
                    if (result.getSaleItemProductUid() != 0) {
                        saleProductUid = result.getSaleItemProductUid();
                    }
                    if (producerUid != 0) {
                        saleProductUid = productUid;
                    }

                    repository.getSaleProductDao().findByUidAsync(saleProductUid,
                            new UmCallback<SaleProduct>() {
                                @Override
                                public void onSuccess(SaleProduct saleProduct) {
                                    String productName = "";
                                    if (saleProduct != null) {
                                        productName = saleProduct.getSaleProductName();
                                    }
                                    view.updateSaleItemOnView(updatedSaleItem,
                                            productName);
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });

                    //Notification observer
                    UmProvider<SaleItemReminder> provider = reminderDao.findBySaleItemUid(saleItemUid);
                    //Testing:
                    List<SaleItemReminder> test = reminderDao.findBySaleItemUidList(saleItemUid);
                    view.setReminderProvider(provider);
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    private void handleSaleItemChanged(SaleItem changedSaleItem){
        if(currentSaleItem == null)
            currentSaleItem = changedSaleItem;

        if(updatedSaleItem == null || !updatedSaleItem.equals(changedSaleItem)) {
            if(changedSaleItem!=null) {

                long saleProductUid = 0;
                if(productUid != 0){
                    saleProductUid = productUid;
                }else if(updatedSaleItem.getSaleItemProductUid() != 0){
                    saleProductUid = updatedSaleItem.getSaleItemProductUid();
                }
                repository.getSaleProductDao().findByUidAsync(saleProductUid
                        , new UmCallback<SaleProduct>() {
                    @Override
                    public void onSuccess(SaleProduct saleProduct) {
                        String productName = "";
                        if(saleProduct != null){
                            productName = saleProduct.getSaleProductName();
                        }
                        view.updateSaleItemOnView(updatedSaleItem, productName);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });


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

            if (updatedSaleItem.getSaleItemQuantity() == 0){
                updatedSaleItem.setSaleItemQuantity(1);
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
        updatedSaleItem.setSaleItemPreorder(!sold);
        updatedSaleItem.setSaleItemSold(sold);
    }
    public void setPreOrder(boolean po){
        updatedSaleItem.setSaleItemSold(!po);
        updatedSaleItem.setSaleItemPreorder(po);
    }

    public void handleChangeOrderDueDate(long date){
        updatedSaleItem.setSaleItemDueDate(date);
    }

    public void handleClickAddReminder() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        if(getArguments().containsKey(ARG_SALE_ITEM_UID)) {
            args.put(ARG_SALE_ITEM_UID, getArguments().get(ARG_SALE_ITEM_UID).toString());
        }
        impl.go(AddReminderDialogView.VIEW_NAME, args, context);


    }

    public void handleDeleteReminder(long saleItemReminderUid) {
        reminderDao.invalidateReminder(saleItemReminderUid, null);
    }

    public void handleAddReminder(int days){
        SaleItemReminder reminder = new SaleItemReminder(days, saleItemUid, true);
        SaleItemReminderDao reminderDao = repository.getSaleItemReminderDao();
        reminderDao.insertAsync(reminder, null);
    }
}
