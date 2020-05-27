package com.ustadmobile.core.view

/**
 * Represents a view interface that has an error notification capability (e.g. Android Snackbar style)
 */
interface UstadBaseFeedbackMessageView : UstadView {

    /**
     * Show a snackbar style notification that an error has happened
     *
     * @param message message to show
     */
    fun showFeedbackMessage(message: String, action: () -> Unit = {}, actionMessageId: Int = 0)

}
