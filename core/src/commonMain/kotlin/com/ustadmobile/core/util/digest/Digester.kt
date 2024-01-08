package com.ustadmobile.core.util.digest

interface Digester {

    fun digest(bytes: ByteArray, offset: Int, len: Int): ByteArray

}