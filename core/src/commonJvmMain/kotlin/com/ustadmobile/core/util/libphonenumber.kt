package com.ustadmobile.core.util

import com.google.i18n.phonenumbers.PhoneNumberUtil


actual fun isValidPhoneNumber(str: String)
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