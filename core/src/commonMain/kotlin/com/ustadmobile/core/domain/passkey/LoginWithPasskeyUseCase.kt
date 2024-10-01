package com.ustadmobile.core.domain.passkey

interface LoginWithPasskeyUseCase {
    suspend operator fun invoke(domain:String): PassKeySignInData?

}