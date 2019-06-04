package com.ustadmobile.port.sharedse.networkmanager

import java.io.IOException
import java.net.URL
import java.net.URLConnection

/**
 * Created by mike on 2/8/18.
 */

class DefaultURLConnectionOpener : URLConnectionOpener {

    @Throws(IOException::class)
    override fun openConnection(url: URL): URLConnection {
        return url.openConnection()
    }
}
