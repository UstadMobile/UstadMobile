package com.ustadmobile.port.sharedse.ext

import fi.iki.elonen.NanoHTTPD

fun newUnsupportedMethodResponse(): NanoHTTPD.Response {
    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
            "text/plain", "Method not allowed")
}