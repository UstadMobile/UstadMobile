package com.ustadmobile.core.view

/**
 * View responsible for seeking student consent before commencing SEL tasks.
 * SELSelectConsent Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SELSelectConsentView : UstadView {

    /**
     * Closes the view
     */
    fun finish()

    /** Notify a message  */
    fun toastMessage(message: String)

    companion object {

        //View name
        val VIEW_NAME = "SELSelectConsent"
    }

}
