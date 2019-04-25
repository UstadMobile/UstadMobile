package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleListDetail;


/**
 * Core View. Screen is for SaleList's View
 */
public interface SaleListView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleList";

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
    void setListProvider(UmProvider<SaleListDetail> listProvider, boolean paymentsDueTab);



    void updateSortSpinner(String[] presets);

    void updatePreOrderCounter(int count);

    void updatePaymentDueCounter(int count);



}

