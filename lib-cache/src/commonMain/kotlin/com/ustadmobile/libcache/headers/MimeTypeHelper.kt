package com.ustadmobile.libcache.headers

interface MimeTypeHelper {

    fun mimeTypeByUri(uri: String): String?

    /**
     * Guess the mime type by extension. The extension should be without the dot
     *
     * @param extension extension without the dot
     */
    fun guessByExtension(extension: String): String?

    companion object {

        val EXTENSION_TO_MIME_TYPE = mapOf(
            "htm" to "text/html",
            "html" to "text/html",
            "xml" to "text/xml",
            "css" to "text/css",
            "asc" to "text/plain",
            "xhtml" to "application/xhtml+xml",
            "txt" to "text/plain",
            "json" to "application/json",


            //Image formats as per https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
            "webp" to "image/webp",
            "gif" to "image/gif",
            "jpg" to "image/jpg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "svg" to "image/svg+xml",
            "apng" to "image/apng",

            "mp3" to "audio/mpeg",
            "m3u" to "audio/mpeg-url",
            "ogg" to "audio/ogg",//See https://wiki.xiph.org/MIME_Types_and_File_Extensions
            "opus" to "audio/ogg",
            "wav" to "audio/wav",

            "avi" to "video/x-msvideo",
            "mp4" to "video/mp4",
            "m4v" to "video/mp4",
            "mkv" to "video/x-matroska",
            "ogv" to "video/ogg",
            "flv" to "video/x-flv",
            "mov" to "video/quicktime",
            "swf" to "application/x-shockwave-flash",
            "mpeg" to "video/mpeg",
            "mpg" to "video/mpeg",
            "webm" to "video/webm",

            "js" to "text/javascript",
            "pdf" to "application/pdf",
            "zip" to "application/zip",
            "epub" to "application/epub+zip",
            "gz" to "application/gzip",

            "otf" to "font/otf",
            "ttf" to "font/ttf",
            "eot" to "application/vnd.ms-fontobject",
            "woff" to "font/woff",
            "woff2" to "font/woff2",
        )

    }

}