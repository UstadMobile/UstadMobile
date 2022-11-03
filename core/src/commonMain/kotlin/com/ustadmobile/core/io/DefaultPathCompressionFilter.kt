package com.ustadmobile.core.io

class DefaultPathCompressionFilter: PathCompressionFilter
{
    override fun getCompressionForPath(pathInContainer: String): ContainerBuilder.Compression {
        return if(pathInContainer.substringAfterLast(".").lowercase()
            in ContainerBuilder.DEFAULT_DONT_COMPRESS_EXTENSIONS
        ) {
            ContainerBuilder.Compression.NONE
        }else {
            ContainerBuilder.Compression.GZIP
        }
    }

}