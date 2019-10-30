package com.ustadmobile.port.android.view.ext

import androidx.paging.PagedList

/**
 * Determine the current active range of a PagedList. This can be used in conjunction with
 * PagedList.Callback to monitor the active range. The PagedList might retain more than the active
 * range if sufficient memory is available.
 *
 * @return Pair(from - inclusive, to - exclusive) indicating the active range of the PagedList.
 */
fun PagedList<*>.activeRange(): Pair<Int, Int> {
    val prefetchDistance = this.config.prefetchDistance
    val lastKey = this.lastKey as Int? ?: 0
    return Pair(Math.max(0, lastKey - prefetchDistance),
            Math.min(lastKey+ prefetchDistance, this.size))
}