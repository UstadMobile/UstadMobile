package com.ustadmobile.core.domain.phonenumber

import com.ustadmobile.core.wrappers.libphonenumber.PhoneNumber

class PhoneNumberJs(
    internal val phoneNumber: PhoneNumber
): IPhoneNumber {

    override val countryCode: Int
        get() = phoneNumber.countryCallingCode.toInt()
    override val nationalNumber: Long
        get() = phoneNumber.nationalNumber.toLong()
}