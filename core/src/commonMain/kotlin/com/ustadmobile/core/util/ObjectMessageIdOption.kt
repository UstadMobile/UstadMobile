package com.ustadmobile.core.util

import org.kodein.di.DI

open class ObjectMessageIdOption<T>(
    messageId: Int,
    context: Any,
    code: Int = messageId,
    val obj: T?,
    di: DI,
    private val displayString: String? = null
) : MessageIdOption(messageId, context, code, di = di) {

    override fun toString(): String {
        return displayString ?: super.toString()
    }

}

