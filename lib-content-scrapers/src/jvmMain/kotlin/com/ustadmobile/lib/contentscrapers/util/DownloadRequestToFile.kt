package com.ustadmobile.lib.contentscrapers.util

import com.ustadmobile.door.ext.writeToFile
import okhttp3.Call
import java.io.File

/**
 * Stream the body of this call to a file
 */
fun Call.downloadToFile(file: File) {
    val body = execute().body ?: throw IllegalStateException("call has no body ${this.request().url}")

    //closing the bytestream will close the call
    body.byteStream().writeToFile(file)
}
