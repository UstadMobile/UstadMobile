package com.ustadmobile.core.network.containerfetcher

import java.net.HttpURLConnection
import java.net.URL

/**
 * On Android Peer-to-Peer downloads it is necessary to open a HttpUrlConnection using the Android
 * network object to force the OS to use the local WiFi network (even though it has no Internet).
 *
 * Otherwise, it is possible to simply use url.openConnection as normal. This interface abstracts
 * this so it can be retrieved by the downloader via Dependency Injection
 */
interface LocalURLConnectionOpener {

    /**
     * Opens an HttpUrlConnection to the given local url (if currently connected to a local network
     * without Internet). Returns null if this is not possible.
     */
    fun openLocalConnection(url: URL): HttpURLConnection?

}