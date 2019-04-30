package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SalePaymentDetailView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SalePayment;

import com.ustadmobile.core.db.dao.SalePaymentDao;

import static com.ustadmobile.core.view.SalePaymentDetailView.ARG_SALE_PAYMENT_DEFAULT_VALUE;
import static com.ustadmobile.core.view.SalePaymentDetailView.ARG_SALE_PAYMENT_UID;

/**
 * Presenter for SalePaymentDetail view
 **/
public class SalePaymentDetailPresenter extends UstadBaseController<SalePaymentDetailView> {


    UmAppDatabase repository;
    private SalePaymentDao paymentDao;

    private long paymentUid;
    private SalePayment currentPayment;

    private long balanceDue;


    public SalePaymentDetailPresenter(Object context, Hashtable arguments, SalePaymentDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        paymentDao = repository.getSalePaymentDao();
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(getArguments().containsKey(ARG_SALE_PAYMENT_UID)){
            paymentUid = Long.parseLong(getArguments().get(ARG_SALE_PAYMENT_UID).toString());
            if(getArguments().containsKey(ARG_SALE_PAYMENT_DEFAULT_VALUE)){
                balanceDue = Long.parseLong(getArguments().get(ARG_SALE_PAYMENT_DEFAULT_VALUE).toString());
            }
            initFromSalePaymentUid(paymentUid);
        }else{
            //Should not happen  \'.'/
        }

    }

    public void initFromSalePaymentUid(long uid){
        paymentDao.findByUidAsync(uid, new UmCallback<SalePayment>() {
            @Override
            public void onSuccess(SalePayment result) {
                currentPayment = result;
                view.runOnUiThread(() -> view.updateSalePaymentOnView(currentPayment));
                if(balanceDue > 0){
                    view.runOnUiThread(() -> view.updateDefaultValue(balanceDue));
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void handleAmountUpdated(long amount){
        currentPayment.setSalePaymentPaidAmount(amount);
    }

    public void handleDateUpdated(long date){
        currentPayment.setSalePaymentPaidDate(date);
    }

    public void handleClickSave(){
        currentPayment.setSalePaymentActive(true);
        currentPayment.setSalePaymentDone(true);
        paymentDao.updateAsync(currentPayment, new UmCallback<Integer>() {
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

}
