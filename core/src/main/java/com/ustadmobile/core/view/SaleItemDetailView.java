package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleItem;


/**
 * Core View. Screen is for SaleItemDetail's View
 */
public interface SaleItemDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleItemDetail";

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
    void setListProvider(UmProvider<SaleItem> listProvider);


}

