package com.ustadmobile.core.util

import org.kodein.di.DI


actual fun isValidPhoneNumber(di: DI, str: String): Boolean {

    if (str.startsWith("+") && str.length > 9 && str.length < 15) {
        return true
    }
    return false
}