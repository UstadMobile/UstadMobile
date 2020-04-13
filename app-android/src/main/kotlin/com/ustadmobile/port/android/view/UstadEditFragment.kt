package com.ustadmobile.port.android.view

import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.port.android.view.ext.saveResultToBackStackSavedStateHandle

abstract class UstadEditFragment<T>: UstadBaseFragment(), UstadEditView<T> {

    override fun finishWithResult(result: List<T>) {
        saveResultToBackStackSavedStateHandle(result)
    }

}