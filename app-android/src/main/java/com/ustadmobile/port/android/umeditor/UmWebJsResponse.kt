package com.ustadmobile.port.android.umeditor

/**
 * Class which represents response received from Javascript method
 * execution or console message on native android.
 *
 * @author kileha3
 */

class UmWebJsResponse {

    /**
     * Get action command to be executed on android native
     */
    /**
     * Set action command to be executed on android native
     * @param action command to be set
     */
    var action: String? = null

    /**
     * Get content received from JS
     * @return received content
     */
    /**
     * Set content to be received to the android native
     * @param content content to be set
     */
    var content: String? = null

    /**
     * Get language directionality
     * @return returns RTL or LTR
     */
    /**
     * Set language directionality
     * @param directionality TRL or LTR
     */
    var directionality: String? = null
}
