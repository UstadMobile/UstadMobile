package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.network.containerfetcher.ConnectionOpener

/**
 * Represents a network manager that can provide a ConnectionOpener to allow the use of a
 * URLConnection tied to the local network (e.g. a WiFi connection with no Internet)
 */
interface NetworkManagerWithConnectionOpener {

    val localConnectionOpener: ConnectionOpener?

}