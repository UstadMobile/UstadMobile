package com.ustadmobile.core.view


/**
 * Core View. Screen is for ChangePassword's View
 */
interface ChangePasswordView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()


    fun sendMessage(messageId: Int)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "ChangePassword"
    }


}

