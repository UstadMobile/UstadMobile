package com.ustadmobile.core.util

import com.ustadmobile.core.impl.UstadMobileSystemImpl

open class MessageIdOption(val messageId: Int, context: Any, val code: Int = messageId) {

    val messageStr = UstadMobileSystemImpl.instance.getString(messageId, context)

    override fun toString(): String = messageStr
}