package com.ustadmobile.core.impl

import com.ustadmobile.core.view.UstadViewWithSnackBar

/**
 * Utility callback that will automatically call the showSnackBarNotification on a view if the
 * callback's onFailure method is called
 *
 * @param <T> Callback type
</T> */
abstract class ShowErrorUmCallback<T>(private val viewFeedback: UstadViewWithSnackBar, private val errorMessage: Int) : UmCallback<T> {

    override fun onFailure(exception: Throwable?) {
        viewFeedback.showSnackBarNotification(UstadMobileSystemImpl.instance.getString(
                errorMessage, viewFeedback.viewContext), { }, 0)
    }
}
