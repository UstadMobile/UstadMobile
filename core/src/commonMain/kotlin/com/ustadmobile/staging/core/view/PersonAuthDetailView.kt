package com.ustadmobile.core.view


/**
 * Core View. Screen is for PersonAuthDetail's View
 */
interface PersonAuthDetailView : UstadView {

    fun updateUsername(username: String)

    fun sendMessage(messageId: Int)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "PersonAuthDetail"

        //Any argument keys:
        val ARG_PERSONAUTH_PERSONUID = "PersonAuthPersonUid"
    }


}

