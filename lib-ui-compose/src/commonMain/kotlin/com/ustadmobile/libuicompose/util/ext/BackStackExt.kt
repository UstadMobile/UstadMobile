package com.ustadmobile.libuicompose.util.ext

import moe.tlaster.precompose.navigation.BackStackEntry

/**
 * Get the destination name as per DEST_NAME constants on the ViewModel
 */
val BackStackEntry.ustadDestName: String
    get() = path.substringAfter("/").substringBefore("?")
