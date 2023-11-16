package com.ustadmobile.libuicompose.util

/**
 * Simple expect/actual that controls how sort options are dislpayed:
 *
 * If use popup is true, then when the user clicks on the sort option, a popup menu of available
 * options appears. Used on Desktop.
 *
 * If false, then a bottom sheet appears. Used on Mobile
 */
expect fun useSortOptionPopup(): Boolean
