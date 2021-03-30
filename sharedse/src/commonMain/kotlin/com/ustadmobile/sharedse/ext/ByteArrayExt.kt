package com.ustadmobile.sharedse.ext

/**
 * Expect/actual that will compress a ByteArray using Gzip
 */
expect fun ByteArray.compressWithGzip(): ByteArray

expect fun ByteArray.decompressWithGzip(): ByteArray
