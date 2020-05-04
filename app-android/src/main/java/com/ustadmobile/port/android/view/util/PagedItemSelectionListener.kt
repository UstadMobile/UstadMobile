package com.ustadmobile.port.android.view.util

import android.view.View

interface PagedItemSelectionListener<T> {

    fun onItemSelectedChanged(view: View, item: T)

}