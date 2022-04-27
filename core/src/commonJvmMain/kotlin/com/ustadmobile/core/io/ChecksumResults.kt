package com.ustadmobile.core.io

/**
 * @param sha256 The SHA256 of the data
 * @param md5 MD5 of the data
 * @param crc32 CRC32 of the data
 */
class ChecksumResults(val sha256: ByteArray, val md5: ByteArray, val crc32: Long)
