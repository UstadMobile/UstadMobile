package com.ustadmobile.port.android.view

import com.ustadmobile.core.view.UstadDetailView

abstract class UstadDetailFragment<T>: UstadBaseFragment(), UstadDetailView<T> {

    override val viewContext: Any
        get() = requireContext()

    override var loading: Boolean = false
        get() = false
        set(value) {
            //TODO: set this on the main activity
            field = value
        }


}