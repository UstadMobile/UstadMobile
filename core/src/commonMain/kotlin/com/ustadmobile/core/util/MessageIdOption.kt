package com.ustadmobile.core.util

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import dev.icerock.moko.resources.StringResource
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

open class MessageIdOption(
    val stringResource: StringResource,
    context: Any,
    val code: Int,
    di: DI
): IdOption("", code) {

    var messageStr = di.direct.instance<UstadMobileSystemImpl>().getString(stringResource)

    override fun toString(): String = messageStr
}