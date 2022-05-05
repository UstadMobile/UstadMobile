package com.ustadmobile.core.util

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

open class MessageIdOption(
    val messageId: Int,
    context: Any,
    val code: Int = messageId,
    di: DI
): IdOption("", code) {

    var messageStr = di.direct.instance<UstadMobileSystemImpl>().getString(messageId, context)

    override fun toString(): String = messageStr
}