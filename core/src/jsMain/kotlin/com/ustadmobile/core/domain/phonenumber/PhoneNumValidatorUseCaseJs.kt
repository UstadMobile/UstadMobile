package com.ustadmobile.core.domain.phonenumber

import com.ustadmobile.core.wrappers.libphonenumber.parsePhoneNumber

class PhoneNumValidatorUseCaseJs: PhoneNumValidatorUseCase {

    override fun isValid(phoneNumber: String): Boolean {
        return try {
            parsePhoneNumber(phoneNumber, null, null).isValid()
        }catch(e: Throwable) {
            false
        }
    }

}