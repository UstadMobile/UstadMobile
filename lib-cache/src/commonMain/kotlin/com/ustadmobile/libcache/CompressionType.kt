package com.ustadmobile.libcache

enum class CompressionType(val headerVal: String) {

    NONE("identity"), GZIP("gzip");

    companion object {

        fun byHeaderVal(headerVal: String?): CompressionType {
            return headerVal?.let { headerStr ->
                entries.firstOrNull { it.headerVal == headerStr }
            } ?: NONE
        }
    }

}