package com.ustadmobile.core.view


/**
 * Core View. Screen is for SelectPersonDialog's View
 */
interface CustomerDetailView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateCustomerName(name:String)
    fun updateLocationName(name:String)
    fun updatePhoneNumber(name:String)

    fun updatePAB(save: Boolean)

    fun updateAndDismiss(personUid: Long, customerName: String)

    fun setLocationPresets(locationPresets: Array<String>, selectedPosition: Int)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "CustomerDetailView"
        const val ARG_CUSTOMER_UID = "CustomerUid";
        const val ARG_CD_LE_UID = "CDLeUid"
    }


}

