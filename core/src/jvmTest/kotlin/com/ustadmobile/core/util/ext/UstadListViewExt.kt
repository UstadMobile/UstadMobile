package com.ustadmobile.core.util.ext

import org.mockito.kotlin.*
import com.ustadmobile.core.view.UstadListView

/**
 * Wait until the initial list of items has been set
 */
fun <DT: Any> UstadListView<*, DT>.waitForListToBeSet(timeoutMillis: Long = 5000L) {
    verify(this, timeout(timeoutMillis).atLeastOnce()).list = any()
}
