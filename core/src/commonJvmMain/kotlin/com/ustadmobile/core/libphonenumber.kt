package com.ustadmobile.core

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

//actual interface PhoneNumberUtil {
//    actual fun display(): String {
//        TODO()
//    }
//}

class MascotImpl: PhoneNumberUtil {
    var phoneUtil: PhoneNumberUtil =
        PhoneNumberUtil.getInstance()
    // it's ok not to implement `display()`: all `actual`s are guaranteed to have a default implementation
}