package com.ustadmobile.core.view

import kotlinx.coroutines.Runnable

/**
 * Represents a view interface that has an error notification capability (e.g. Android Snackbar style)
 */
interface ViewWithErrorNotifier : UstadView {

    /**
     * Show a snackbar style notification that an error has happened
     *
     * @param errorMessage Error message to show
     */
    fun showErrorNotification(errorMessage: String, action: Runnable?, actionMessageId: Int?)

}
