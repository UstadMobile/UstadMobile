package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.DateRange;

/**
 * Core View. Screen is for AddDateRangeDialog's View
 */
public interface AddDateRangeDialogView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "AddDateRangeDialog";

    //Any argument keys:
    String DATERANGE_UID = "DateRangeUid";

    /**
     * Method to finish the screen / view.
     */
    void finish();

    void updateFields(DateRange dateRange);

    /**
     * Sets an error on the dialog if the input wasn't valid.
     *
     * @param errorMessage  the error message you want to display in the dialog.
     */
    void setError(String errorMessage);


}

