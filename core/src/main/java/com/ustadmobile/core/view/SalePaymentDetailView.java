package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SalePayment;


/**
 * Core View. Screen is for SalePaymentDetail's View
 */
public interface SalePaymentDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SalePaymentDetail";

    //Any argument keys:
    String ARG_SALE_PAYMENT_UID = "ArgSalePaymentUid";

    /**
     * Method to finish the screen / view.
     */
    void finish();

    void updateSalePaymentOnView(SalePayment payment);

}

