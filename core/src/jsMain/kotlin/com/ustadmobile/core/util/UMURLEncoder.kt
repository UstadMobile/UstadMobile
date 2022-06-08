package com.ustadmobile.core.util

/**
 * External JS function to encode URI
 */
external fun encodeURI(uri: String?): String

external fun encodeURIComponent(uriComponent: String?): String

/**
 * External JS function to decode URI
 */
external fun decodeURI(uri: String?): String

external fun decodeURIComponent(uriComponent: String?): String

actual class UMURLEncoder {

    actual companion object {
        /**
         * Encode url string
         */
        actual fun encodeUTF8(text: String): String {
            return encodeURIComponent(text)
        }

        /**
         * Decode url string
         */
        actual fun decodeUTF8(text: String): String {
            return decodeURIComponent(text)
        }

    }

}