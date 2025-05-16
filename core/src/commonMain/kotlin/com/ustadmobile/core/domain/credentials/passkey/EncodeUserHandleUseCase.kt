package com.ustadmobile.core.domain.credentials.passkey


interface EncodeUserHandleUseCase {
    operator fun invoke(personPasskeyUid:Long): String
}