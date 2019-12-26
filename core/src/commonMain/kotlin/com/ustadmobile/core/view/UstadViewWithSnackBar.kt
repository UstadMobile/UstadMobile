package com.ustadmobile.core.view

/**
 * Represents a view interface that has an error notification capability (e.g. Android Snackbar style)
 */
interface UstadViewWithSnackBar : UstadView {

    /**
     * Show a snackbar style notification that an error has happened
     *
     * @param snackBarMessage message to show
     */
    fun showSnackBarNotification(snackBarMessage: String, action: () -> Unit, actionMessageId: Int)

}
