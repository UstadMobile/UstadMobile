package com.ustadmobile.core.util.digest

actual fun Digester(algoName: String) : Digester{
    throw IllegalStateException("Not supported on JS")
}
