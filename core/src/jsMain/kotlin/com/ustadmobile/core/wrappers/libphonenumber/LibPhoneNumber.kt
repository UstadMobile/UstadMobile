@file:JsModule("libphonenumber-js")
@file:JsNonModule

package com.ustadmobile.core.wrappers.libphonenumber

@Suppress("unused")
external interface ParsePhoneNumberOptions {
    var defaultCountry: String
    var extract: Boolean
}

//As per https://www.npmjs.com/package/libphonenumber-js#phonenumber
@Suppress("unused")
external class PhoneNumber {
    val number: String
    val countryCallingCode: String
    val nationalNumber: String
    val country: String?
    val ext: String?
    val carrierCode: String?

    fun setExt(ext: String)

    fun isValid(): Boolean
    fun isPossible(): Boolean


    fun formatInternational(): String

    fun formatNational(): String

    fun getURI(): String

}

external fun parsePhoneNumber(
    phoneNum: String,
    defaultCountry: String?,
    options: ParsePhoneNumberOptions?
): PhoneNumber
