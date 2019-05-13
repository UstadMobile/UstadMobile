package com.ustadmobile.core.impl

import com.ustadmobile.core.view.ViewWithErrorNotifier
import kotlinx.coroutines.Runnable

/**
 * Utility callback that will automatically call the showErrorNotification on a view if the
 * callback's onFailure method is called
 *
 * @param <T> Callback type
</T> */
abstract class ShowErrorUmCallback<T>(private val view: ViewWithErrorNotifier, private val errorMessage: Int) : UmCallback<T> {

    override fun onFailure(exception: Throwable?) {
        view.showErrorNotification(UstadMobileSystemImpl.instance.getString(
                errorMessage, view.context), Runnable { }, 0)
    }
}
