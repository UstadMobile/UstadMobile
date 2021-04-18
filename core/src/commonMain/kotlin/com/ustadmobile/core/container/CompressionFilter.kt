package com.ustadmobile.core.container

interface CompressionFilter {

    /**
     * Determine if the given entry should be compressed
     *
     * @param pathInContainer the path that this entry will be given in the container
     * @param mimeType the mime type, if known
     *
     * @return true if it should be compressed, false otherwise
     */
    fun shouldCompress(pathInContainer: String, mimeType: String?): Boolean

}