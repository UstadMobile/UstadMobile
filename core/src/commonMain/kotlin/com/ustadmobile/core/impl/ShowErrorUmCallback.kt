package com.ustadmobile.core.impl

import com.ustadmobile.core.view.UstadBaseFeedbackMessageView

/**
 * Utility callback that will automatically call the showSnackBarNotification on a view if the
 * callback's onFailure method is called
 *
 * @param <T> Callback type
</T> */
abstract class ShowErrorUmCallback<T>(private val baseFeedbackMessageViewFeedback: UstadBaseFeedbackMessageView, private val errorMessage: Int) : UmCallback<T> {

    override fun onFailure(exception: Throwable?) {
        baseFeedbackMessageViewFeedback.showFeedbackMessage(UstadMobileSystemImpl.instance.getString(
                errorMessage, baseFeedbackMessageViewFeedback.viewContext), { }, 0)
    }
}
