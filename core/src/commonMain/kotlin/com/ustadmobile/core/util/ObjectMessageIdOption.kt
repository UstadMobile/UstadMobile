package com.ustadmobile.core.util

import dev.icerock.moko.resources.StringResource
import org.kodein.di.DI

open class ObjectMessageIdOption<T>(
    stringResource: StringResource,
    context: Any,
    code: Int,
    val obj: T?,
    di: DI,
    private val displayString: String? = null
) : MessageIdOption(stringResource, context, code, di = di) {

    override fun toString(): String {
        return displayString ?: super.toString()
    }

}

