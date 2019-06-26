package com.ustadmobile.core.view


/**
 * Core View. Screen is for SelectLanguageDialog's View
 */
interface SelectLanguageDialogView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectLanguageDialog"
    }


}

