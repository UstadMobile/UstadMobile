package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.DateRange

/**
 * Core View. Screen is for AddDateRangeDialog's View
 */
interface AddDateRangeDialogView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateFields(dateRange: DateRange)

    /**
     * Sets an error on the dialog if the input wasn't valid.
     *
     * @param errorMessage  the error message you want to display in the dialog.
     */
    fun setError(errorMessage: String)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "AddDateRangeDialog"

        //Any argument keys:
        val DATERANGE_UID = "DateRangeUid"
    }


}

