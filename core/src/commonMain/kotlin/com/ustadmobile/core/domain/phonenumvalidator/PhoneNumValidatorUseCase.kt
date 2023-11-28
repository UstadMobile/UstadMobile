package com.ustadmobile.core.domain.phonenumvalidator

interface PhoneNumValidatorUseCase {

    fun isValid(phoneNumber: String): Boolean

}