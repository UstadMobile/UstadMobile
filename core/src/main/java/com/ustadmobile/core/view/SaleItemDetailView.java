package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.SaleItem;


/**
 * Core View. Screen is for SaleItemDetail's View
 */
public interface SaleItemDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleItemDetail";

    //Any argument keys:
    String ARG_SALE_ITEM_UID = "ArgSaleItemUid";
    String ARG_SALE_ITEM_PRODUCT_UID = "ArgSaleItemProducerUid";


    /**
     * Method to finish the screen / view.
     */
    void finish();

    void updateSaleItemOnView(SaleItem saleItem, String productTitle);

    void updateTotal(long total);

    void updatePPP(long ppp);

    void showPreOrder(boolean show);

}

