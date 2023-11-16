package com.ustadmobile.libuicompose.util

import com.ustadmobile.libuicompose.components.SortListMode

/**
 * Simple expect/actual that controls how sort options are dislpayed:
 *
 * If use popup is true, then when the user clicks on the sort option, a popup menu of available
 * options appears. Used on Desktop.
 *
 * If false, then a bottom sheet appears. Used on Mobile
 */
actual fun defaultSortListMode(): SortListMode = SortListMode.POPUP
