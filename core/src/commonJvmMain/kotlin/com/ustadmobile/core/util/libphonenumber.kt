package com.ustadmobile.core.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ustadmobile.core.impl.CountryProviderImp
import org.kodein.di.instance
import org.kodein.di.DI

actual fun isValidPhoneNumber(di: DI, str: String): Boolean {

    val phoneUtil = PhoneNumberUtil.getInstance()

    val impl: CountryProviderImp by di.instance()

    try {
        val numberProto = phoneUtil.parse(str, impl.getCountry().countryIsoCode)
        return phoneUtil.isValidNumber(numberProto)
    } catch (e: NumberParseException) {
        System.err.println("NumberParseException was thrown: $e")
    }
    return false
}