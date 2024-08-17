package com.ustadmobile.core.domain.passkey

interface CreatePasskeyUseCase {
    suspend operator fun invoke(createPassKeyParams:CreatePasskeyParams): PasskeyResult?

}