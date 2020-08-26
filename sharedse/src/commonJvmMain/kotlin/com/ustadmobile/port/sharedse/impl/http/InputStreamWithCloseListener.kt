package com.ustadmobile.port.sharedse.impl.http

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

import fi.iki.elonen.NanoHTTPD

/**
 * A simple FilterInputStream that is used so an event can be received when the underlying stream
 * is closed.
 */
class InputStreamWithCloseListener(inputStream: InputStream, @field:Volatile private var onCloseListener: OnCloseListener?) : FilterInputStream(inputStream) {

    /**
     * A listener that will receive an event notification when the stream is closed.
     */
    interface OnCloseListener {

        /**
         * Event that signifies the stream has been closed. It will be called only once no matter
         * how many times the underlying stream is closed.
         */
        fun onStreamClosed()
    }


    @Throws(IOException::class)
    override fun close() {
        super.close()
        onCloseListener?.onStreamClosed()
        onCloseListener = null
    }
}
