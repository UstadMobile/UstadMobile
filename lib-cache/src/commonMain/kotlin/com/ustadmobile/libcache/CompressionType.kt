package com.ustadmobile.libcache

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