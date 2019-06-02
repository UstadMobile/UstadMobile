package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;
import com.ustadmobile.lib.db.entities.SalePayment;


/**
 * Core View. Screen is for SaleDetail's View
 */
public interface SaleDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleDetail";

    //Any argument keys:
    String ARG_SALE_UID = "ArgSaleUid";

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


    void setLocationPresets(String[] locationPresets, int selectedPosition);

    /**
     * Payment provider for this sale.
     *
     * @param paymentProvider   the provider to set to the view
     */
    void setPaymentProvider(UmProvider<SalePayment> paymentProvider);

    void updateOrderTotal(long orderTotal);

    void updateOrderTotalAfterDiscount(long discount);
    void updateOrderTotalAfterDiscountTotalChanged(long total);


    void updateSaleOnView(Sale sale);

    //N/A:
    void updatePaymentTotal(long paymentTotal);

    void showSaveButton(boolean show);
    void showCalculations(boolean show);
    void showDelivered(boolean show);
    void showNotes(boolean show);
    void showSignature(boolean show);
    void showPayments(boolean show);

    void updateSaleVoiceNoteOnView(String fileName);

    void updateBalanceDue(long balance);


}

