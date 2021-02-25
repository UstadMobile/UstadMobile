package com.ustadmobile.core.util

open class ObjectMessageIdOption<T>(messageId: Int, context: Any, code: Int = messageId,
                                    val obj: T?, private val displayString: String? = null) :
        MessageIdOption(messageId, context, code) {

    override fun toString(): String {
        return displayString ?: super.toString()
    }

}

