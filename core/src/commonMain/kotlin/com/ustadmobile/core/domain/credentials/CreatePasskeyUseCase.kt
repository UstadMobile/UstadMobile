package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.domain.credentials.passkey.webAuthn.PasskeyWebAuthNResponse

interface CreatePasskeyUseCase {

    suspend operator fun invoke(createPassKeyParams:CreatePasskeyParams): PasskeyWebAuthNResponse

}