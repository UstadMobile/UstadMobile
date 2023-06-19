package com.ustadmobile.core.util

import org.kodein.di.DI


actual fun isValidPhoneNumber(di: DI, str: String): Boolean {

    val regex = "^\\+(?:[0-9]●?){6,14}[0-9]\$"

    if (str.toRegex().matches(regex)) {
        return true
    }
    return false
}