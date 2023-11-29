package com.ustadmobile.libuicompose.util.phonenum

import android.telephony.PhoneNumberUtils

actual fun isNonSeparator(char: Char): Boolean {
    return PhoneNumberUtils.isNonSeparator(char)
}

actual fun isReallyDialable(char: Char): Boolean {
    return PhoneNumberUtils.isReallyDialable(char)
}
