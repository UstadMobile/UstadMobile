package com.ustadmobile.core.view;


/**
 * Core View. Screen is for LocationDetail's View
 */
public interface LocationDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "LocationDetail";

    //Any argument keys:
    String LOCATION_UID = "LocationUid";

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

