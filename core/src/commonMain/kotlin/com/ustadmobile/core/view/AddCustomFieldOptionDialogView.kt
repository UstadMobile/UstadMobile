package com.ustadmobile.core.view


/**
 * Core View. Screen is for AddCustomFieldOptionDialogView's View
 */
interface AddCustomFieldOptionDialogView : UstadView {
    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun setOptionValue(optionValue: String)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "AddCustomFieldOptionDialogView"

        //Any argument keys:
        val ARG_CUSTOM_FIELD_VALUE_OPTION_UID = "CustomFieldOptionUid"
    }


}

