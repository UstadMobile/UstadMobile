package com.ustadmobile.core.domain.phonenumber

import io.michaelrocks.libphonenumber.android.Phonenumber.PhoneNumber

class PhoneNumberAndroid(
    internal val phoneNumber: PhoneNumber
) : IPhoneNumber {
    override val countryCode: Int
        get() = phoneNumber.countryCode
    override val nationalNumber: Long
        get() = phoneNumber.nationalNumber
}