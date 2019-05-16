package com.ustadmobile.door

import kotlin.jvm.JvmStatic

class DoorDbType {

    companion object {

        const val SQLITE = 1

        const val POSTGRES = 2

        val SUPPORTED_TYPES = listOf(SQLITE, POSTGRES)

        @JvmStatic
        val PRODUCT_NAME_MAP = mapOf("POSTGRES" to SQLITE,
                "SQLITE" to SQLITE);

        fun typeIntFromProductName(productName: String) = PRODUCT_NAME_MAP[productName] ?: -1

    }
}

