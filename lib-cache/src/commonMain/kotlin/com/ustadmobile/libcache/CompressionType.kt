package com.ustadmobile.libcache

/**
 * Supports Gzip and Identity (e.g. not compressed). It would be nice to add Brotli, unfortunately,
 * there is no Java/Kotlin encoder available as per :
 *
 *  https://github.com/google/brotli/issues/405
 *
 *  Could potentially use the JNI wrapper and compile for Android:
 *  https://github.com/google/brotli/blob/master/java/org/brotli/wrapper/enc/Encoder.java
 */
enum class CompressionType(
    val headerVal: String,
) {

    NONE("identity"), GZIP("gzip");

    companion object {

        fun parseAcceptEncodingHeader(headerVal: String?): List<CompressionType> {
            return headerVal?.split(",")
                ?.mapNotNull {
                    byHeaderValOrNull(it.substringBefore(";").trim())
                } ?: listOf(NONE)
        }

        /**
         *
         */
        fun byHeaderValOrNull(headerVal: String?): CompressionType? {
            return headerVal?.let { headerStr ->
                entries.firstOrNull { it.headerVal == headerStr }
            }
        }

        fun byHeaderVal(headerVal: String?): CompressionType {
            return headerVal?.let { headerStr ->
                entries.firstOrNull { it.headerVal == headerStr }
            } ?: NONE
        }
    }

}