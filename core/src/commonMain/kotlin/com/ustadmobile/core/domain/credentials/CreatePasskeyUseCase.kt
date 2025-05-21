package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON

interface CreatePasskeyUseCase {

    suspend operator fun invoke(username:String): AuthenticationResponseJSON

}