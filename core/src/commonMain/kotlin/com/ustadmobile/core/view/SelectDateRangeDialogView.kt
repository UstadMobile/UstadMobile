package com.ustadmobile.core.view


/**
 * Core View. Screen is for SelectDateRangeDialog's View
 */
interface SelectDateRangeDialogView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectDateRangeDialog"

        //Any argument keys:
        const val ARG_FROM_DATE = "ArgFromDate"
        const val ARG_TO_DATE = "ArgToDate"
    }


}

