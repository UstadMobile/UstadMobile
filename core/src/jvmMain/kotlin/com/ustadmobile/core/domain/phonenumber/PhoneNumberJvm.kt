package com.ustadmobile.core.domain.phonenumber

import com.google.i18n.phonenumbers.Phonenumber

class PhoneNumberJvm(
    internal val phoneNumber: Phonenumber.PhoneNumber
) : IPhoneNumber {

    override val countryCode: Int
        get() = phoneNumber.countryCode
    override val nationalNumber: Long
        get() = phoneNumber.nationalNumber
}
