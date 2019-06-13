package com.ustadmobile.sharedse.io

import kotlinx.io.InputStream

expect class GzipInputStreamSe {

    constructor(input: InputStream)

    constructor(input: InputStream, size: Int)

    fun read(b: ByteArray): Int

}