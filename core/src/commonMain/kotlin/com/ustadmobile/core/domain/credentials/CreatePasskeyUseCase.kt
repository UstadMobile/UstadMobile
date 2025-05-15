package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.domain.credentials.passkey.webAuthn.AuthenticationResponseJSON

interface CreatePasskeyUseCase {

    suspend operator fun invoke(username:String): AuthenticationResponseJSON

}