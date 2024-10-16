package com.ustadmobile.core.domain.phonenumber


class PhoneNumValidatorJvm(
    private val iPhoneNumberUtil: IPhoneNumberUtil
): PhoneNumValidatorUseCase {
    override fun isValid(phoneNumber: String): Boolean {
        return try {
            iPhoneNumberUtil.isValidNumber(iPhoneNumberUtil.parse(phoneNumber, "US"))
        }catch(e: Throwable) {
            false
        }
    }

}