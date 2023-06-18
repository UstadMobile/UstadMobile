package com.ustadmobile.core.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ustadmobile.core.impl.CountryProviderImp
import org.kodein.di.instance

actual fun isValidPhoneNumber(str: String): Boolean {

    val phoneUtil = PhoneNumberUtil.getInstance()

    val impl: CountryProviderImp by instance()

    try {
        val swissNumberProto = phoneUtil.parse(str, impl.getCountry().countryIsoCode)
        return phoneUtil.isValidNumber(swissNumberProto)
    } catch (e: NumberParseException) {
        System.err.println("NumberParseException was thrown: $e")
    }
    return false
}