package com.ustadmobile.core.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil


actual fun isValidPhoneNumber(str: String): Boolean {

    val phoneUtil = PhoneNumberUtil.getInstance()
    try {
        val swissNumberProto = phoneUtil.parse(str, deviceIsoCode())
        return phoneUtil.isValidNumber(swissNumberProto)
    } catch (e: NumberParseException) {
        System.err.println("NumberParseException was thrown: $e")
    }
    return false
}