package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.port.sharedse.ext.dataInflatedIfRequired
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD

/**
 * This class is designed to filter out VH units and convert them to pixels. This is required because
 * the WebView displaying EPUB content in the recyclerview has it's height set to wrap_content. This
 * results in the WebView interpreting any vh unit as zero.
 */
class CssVhFilter (var vhToPxFactor: () -> Float): MountedContainerResponder.MountedContainerFilter{

    override fun filterResponse(
        responseIn: NanoHTTPD.Response,
        uriResource: RouterNanoHTTPD.UriResource,
        urlParams: Map<String, String>, session
        : NanoHTTPD.IHTTPSession
    ): NanoHTTPD.Response {
        val contentType = responseIn.getMimeType()
        if(contentType.startsWith("text/css")) {
            var cssText = responseIn.dataInflatedIfRequired().bufferedReader().use {
                it.readText()
            }

            var pos = 0
            while(pos < cssText.length - 1) {
                if(cssText[pos] == 'v' && cssText[pos + 1] == 'h') {
                    //we have found the pesky vh unit - let's find the number and replace it

                    val valueStart = cssText.lastIndexOfAnyMatching(pos - 1) {
                        !(it.isWhitespace() || it.isDigit() || it == '.')
                    } + 1

                    //Make sure there is an actual value to change here
                    if(pos - valueStart  > 2) {
                        val vhNumericalVal = cssText.substring(valueStart, pos).trim().toFloat()
                        val replacement = "${vhNumericalVal * vhToPxFactor()}px"
                        cssText = cssText.substring(0, valueStart) + replacement + cssText.substring(pos + 2)

                        //the length of the string has changed. We need to update our position accordingly
                        pos += (replacement.length - ((pos + 1) - valueStart))
                    }
                }

                pos++
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                "text/css", cssText)
        }else {
            return responseIn
        }

    }

    inline fun String.lastIndexOfAnyMatching(fromPos: Int, checker: (Char) -> Boolean) : Int {
        for(i in fromPos downTo 0) {
            if(checker(this[i]))
                return i
        }

        return -1
    }

}