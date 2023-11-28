package com.ustadmobile.core.domain.phonenumvalidator

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.wrappers.libphonenumber.parsePhoneNumber

class PhoneNumValidatorJs: PhoneNumValidatorUseCase {

    override fun isValid(phoneNumber: String): Boolean {
        return try {
            parsePhoneNumber(phoneNumber, null, null).isValid()
        }catch(e: Throwable) {
            false
        }
    }

}