package com.ustadmobile.core.util.ext

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ExceptionWithStringResource

fun Throwable.stringResourceOrMessage(systemImpl: UstadMobileSystemImpl): String {
    return (this as? ExceptionWithStringResource)?.stringResource?.let {
        systemImpl.getString(it)
    } ?: "${systemImpl.getString(MR.strings.error)}: ${message ?: ""}"
}
