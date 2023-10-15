package com.ustadmobile.libcache.headers

interface MimeTypeHelper {

    fun mimeTypeByUri(uri: String): String?

    companion object {

        val EXTENSION_TO_MIME_TYPE = mapOf(
            "htm" to "text/html",
            "html" to "text/html",
            "xml" to "text/xml",
            "css" to "text/css",
            "asc" to "text/plain",
            "xhtml" to "application/xhtml+xml",
            "txt" to "text/plain",

            "webp" to "image/webp",
            "webm" to "image/webm",
            "gif" to "image/gif",
            "jpg" to "image/jpg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "svg" to "image/svg+xml",

            "mp3" to "audio/mpeg",
            "m3u" to "audio/mpeg-url",
            "ogg" to "audio/ogg",//See https://wiki.xiph.org/MIME_Types_and_File_Extensions
            "opus" to "audio/ogg",
            "wav" to "audio/wav",

            "mp4" to "video/mp4",
            "m4v" to "video/mp4",
            "ogv" to "video/ogg",
            "flv" to "video/x-flv",
            "mov" to "video/quicktime",
            "swf" to "application/x-shockwave-flash",

            "js" to "application/javascript",
            "pdf" to "application/pdf",
            "zip" to "application/zip",
            "epub" to "application/epub+zip",
            "gz" to "application/gzip",
        )

    }

}