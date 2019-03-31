package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;
import com.ustadmobile.lib.db.entities.SalePayment;


/**
 * Core View. Screen is for SaleDetail's View
 */
public interface SaleDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleDetail";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    void setListProvider(UmProvider<SaleItemListDetail> listProvider);


    /**
     * Payment provider for this sale.
     *
     * @param paymentProvider   the provider to set to the view
     */
    void setPaymentProvider(UmProvider<SalePayment> paymentProvider);


}

