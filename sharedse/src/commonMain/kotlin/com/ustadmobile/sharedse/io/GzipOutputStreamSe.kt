package com.ustadmobile.sharedse.io

import kotlinx.io.OutputStream


expect class GzipOutputStreamSe {

    constructor(out: OutputStream)

    fun write(b: ByteArray)

    fun close()

    fun flush()

}