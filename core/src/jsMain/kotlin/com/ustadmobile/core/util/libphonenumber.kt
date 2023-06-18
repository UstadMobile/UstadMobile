package com.ustadmobile.core.util

import org.kodein.di.DI


@JsModule("libphonenumber-js")
@JsNonModule
external fun parsePhoneNumber(number: String, countryCode: String): dynamic

@JsModule("libphonenumber-js")
@JsNonModule
external fun isValidNumber(number: String): Boolean

actual fun isValidPhoneNumber(di: DI, str: String): Boolean {

    val phoneNumber = parsePhoneNumber(str, "CH")
    if (!(phoneNumber as? String).isNullOrBlank()) {
        return phoneNumber.isValid() == true
    }
    return false
}