package com.ustadmobile.core.util.ext

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.view.UstadListView

/**
 * Wait until the initial list of items has been set
 */
fun <DT> UstadListView<*, DT>.waitForListToBeSet(timeoutMillis: Long = 5000L) {
    verify(this, timeout(timeoutMillis).atLeastOnce()).list = any()
}
