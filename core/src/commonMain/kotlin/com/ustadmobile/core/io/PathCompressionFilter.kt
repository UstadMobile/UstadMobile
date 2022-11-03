package com.ustadmobile.core.io

fun interface PathCompressionFilter {

    fun getCompressionForPath(pathInContainer: String): ContainerBuilder.Compression

}
