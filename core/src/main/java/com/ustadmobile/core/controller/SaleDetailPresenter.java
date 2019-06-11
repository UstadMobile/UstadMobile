package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.db.dao.SalePaymentDao;
import com.ustadmobile.core.db.dao.SaleVoiceNoteDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.SaleDetailSignatureView;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.core.view.SalePaymentDetailView;
import com.ustadmobile.core.view.SelectProducerView;
import com.ustadmobile.core.view.SelectSaleProductView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItem;

import com.ustadmobile.core.db.dao.SaleItemDao;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;
import com.ustadmobile.lib.db.entities.SalePayment;
import com.ustadmobile.lib.db.entities.SaleVoiceNote;

import jdk.nashorn.internal.runtime.UserAccessorProperty;

import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;
import static com.ustadmobile.core.view.SaleItemDetailView.ARG_SALE_ITEM_UID;
import static com.ustadmobile.core.view.SalePaymentDetailView.ARG_SALE_PAYMENT_DEFAULT_VALUE;
import static com.ustadmobile.core.view.SalePaymentDetailView.ARG_SALE_PAYMENT_UID;

/**
 * Presenter for SaleDetail view
 **/
public class SaleDetailPresenter extends UstadBaseController<SaleDetailView> {

    private UmProvider<SaleItemListDetail> umProvider;
    private UmProvider<SalePayment> pProvider;
    UmAppDatabase repository;
    private SaleItemDao saleItemDao;
    private SaleDao saleDao;
    private SaleVoiceNoteDao saleVoiceNoteDao;
    private SalePaymentDao salePaymentDao;
    private SaleItem currentSaleItem = null;
    private Sale currentSale;
    private Sale updatedSale;
    private LocationDao locationDao;

    private UmLiveData<List<Location>> locationLiveData;

    private Hashtable<Integer, Long> positionToLocationUid;

    private boolean showSaveButton = false;

    private String voiceNoteFileName;
    private long totalPayment=0;
    private long totalAfterDiscount = 0;

    public SaleDetailPresenter(Object context, Hashtable arguments, SaleDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleItemDao = repository.getSaleItemDao();
        salePaymentDao = repository.getSalePaymentDao();
        saleDao = repository.getSaleDao();
        locationDao = repository.getLocationDao();
        saleVoiceNoteDao = UmAppDatabase.getInstance(context).getSaleVoiceNoteDao();

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
                view.showSignature(true);
                view.showDelivered(true);
                view.showNotes(true);
                view.showPayments(true);
            });

        }else{
            view.runOnUiThread(() -> view.showPayments(false));
            updatedSale = new Sale();
            updatedSale.setSalePreOrder(true); //ie: Not delivered unless ticked.
            updatedSale.setSaleDone(false);
            updatedSale.setSaleActive(false);
            view.showSignature(false);

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

    public void updatePaymentItemProvider(long saleUid){
        //Get provider
        pProvider = salePaymentDao.findBySaleProvider(saleUid);
        view.setPaymentProvider(pProvider);
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
                        view.showPayments(true);
                    });
                }

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
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

    //Called every time payment list gets updated (via Recycler Adapter's custom observer)
    public void getTotalPaymentsAndUpdateTotalView(long saleUid){
        //Get total payment count
        salePaymentDao.findTotalPaidBySaleAsync(saleUid, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                // Then update totals
                totalPayment= result;
                updateBalance();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    public void updateBalanceDueFromTotal(float totalAD){
        totalAfterDiscount = (long) totalAD;
        updateBalance();

    }

    public void updateBalance(){
        view.updateBalanceDue(totalAfterDiscount - totalPayment);
    }

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


        //Any voice notes
        saleVoiceNoteDao.findBySaleUidAsync(saleUid, new UmCallback<SaleVoiceNote>() {
            @Override
            public void onSuccess(SaleVoiceNote result) {
                if(result!= null){
                    String voiceNotePath  = saleVoiceNoteDao.getAttachmentPath(result.getSaleVoiceNoteUid());
                    if(voiceNotePath!=null&&!voiceNotePath.isEmpty()){
                        view.updateSaleVoiceNoteOnView(voiceNotePath);
                    }
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        getTotalSaleOrderAndDiscountAndUpdateView(saleUid);
        updateSaleItemProvider(saleUid);
        updatePaymentItemProvider(saleUid);

        getPaymentTotalAndUpdateView();
    }



    private void startObservingLocations(){
        locationLiveData = locationDao.findAllActiveLocationsLive();
        locationLiveData.observe(SaleDetailPresenter.this,
                SaleDetailPresenter.this::handleLocationsChanged);
    }

    private void handleSaleChanged(Sale sale){
        //set the og person value
        if(currentSale == null)
            currentSale = sale;

        if(updatedSale == null || !updatedSale.equals(sale)) {
            if(sale!=null) {
                updatedSale = sale;
                view.updateSaleOnView(updatedSale);
            }
        }

    }

    public void refreshSaleOnView(){
        view.updateSaleOnView(updatedSale);
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
            if(updatedSale.getSaleLocationUid() == 0){
                updatedSale.setSaleLocationUid(positionToLocationUid.get(0));
            }

            //Persist voice note
            if(voiceNoteFileName != null && !voiceNoteFileName.isEmpty()){
                SaleVoiceNote voiceNote = new SaleVoiceNote();
                voiceNote.setSaleVoiceNoteSaleUid(updatedSale.getSaleUid());
                saleVoiceNoteDao.insertAsync(voiceNote, new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        try {
                            FileInputStream is = new FileInputStream(voiceNoteFileName);
                            saleVoiceNoteDao.setAttachment(result,is);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

            }

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

    public void handleClickAddPayment(){
        SalePayment newSalePayment = new SalePayment();
        newSalePayment.setSalePaymentActive(false);
        newSalePayment.setSalePaymentPaidDate(System.currentTimeMillis()); //default start to today
        newSalePayment.setSalePaymentPaidAmount(0);
        newSalePayment.setSalePaymentCurrency("Afs");
        newSalePayment.setSalePaymentSaleUid(updatedSale.getSaleUid());
        newSalePayment.setSalePaymentDone(false);
        salePaymentDao.insertAsync(newSalePayment, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                newSalePayment.setSalePaymentUid(result);

                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                Hashtable<String, String> args = new Hashtable<>();
                args.put(ARG_SALE_PAYMENT_UID, String.valueOf(newSalePayment.getSalePaymentUid()));
                args.put(ARG_SALE_PAYMENT_DEFAULT_VALUE, String.valueOf(totalAfterDiscount - totalPayment));
                impl.go(SalePaymentDetailView.VIEW_NAME, args, context);

            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

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

    public String getVoiceNoteFileName() {
        return voiceNoteFileName;
    }

    public void setVoiceNoteFileName(String voiceNoteFileName) {
        this.voiceNoteFileName = voiceNoteFileName;
    }

    public void handleDeleteVoiceNote(){
        this.voiceNoteFileName = "";
    }

    public void handleDeletePayment(long salePaymentUid){
        salePaymentDao.inactivateEntityAsync(salePaymentUid, null);
    }

    public void handleEditPayment(long salePaymentUid){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_PAYMENT_UID, String.valueOf(salePaymentUid));
        //args.put(ARG_SALE_PAYMENT_DEFAULT_VALUE, String.valueOf(totalAfterDiscount - totalPayment));
        impl.go(SalePaymentDetailView.VIEW_NAME, args, context);
    }

    public void handleClickAddSignature() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_SALE_UID, String.valueOf(currentSale.getSaleUid()));
        impl.go(SaleDetailSignatureView.VIEW_NAME, args, context);
    }
}
