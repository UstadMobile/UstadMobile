package com.ustadmobile.core.domain.phonenumber

fun IPhoneNumberUtil.formatInternationalOrNull(number: String): String? {
    return try {
        formatInternational(parse(number, "US"))
    }catch(e: Throwable) {
        null
    }
}