package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Sale;


/**
 * Core View. Screen is for SaleDetailSignatureView's View
 */
public interface SaleDetailSignatureView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleDetailSignatureView";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();



}

