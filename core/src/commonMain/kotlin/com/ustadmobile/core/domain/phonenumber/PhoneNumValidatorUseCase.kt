package com.ustadmobile.core.domain.phonenumber

interface PhoneNumValidatorUseCase {

    fun isValid(phoneNumber: String): Boolean

}