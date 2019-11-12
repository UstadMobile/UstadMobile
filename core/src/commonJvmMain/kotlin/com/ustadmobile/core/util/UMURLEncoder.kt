package com.ustadmobile.core.util

import kotlin.jvm.JvmStatic

actual class UMURLEncoder {
    actual companion object {
        /**
         * Encode url string
         */
        @JvmStatic
        actual fun encodeUTF8(text: String): String {
            return java.net.URLEncoder.encode(text, "UTF-8")
        }

        /**
         * Decode url string
         */
        @JvmStatic
        actual fun decodeUTF8(text: String): String {
            return java.net.URLDecoder.decode(text, "UTF-8")
        }

    }
}