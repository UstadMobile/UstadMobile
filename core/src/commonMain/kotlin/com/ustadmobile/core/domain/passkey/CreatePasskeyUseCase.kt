package com.ustadmobile.core.domain.passkey

import com.ustadmobile.lib.db.entities.Person

interface CreatePasskeyUseCase {

    suspend operator fun invoke(createPassKeyParams:CreatePasskeyParams): CreatePasskeyResult?


    data class CreatePasskeyResult(
        val attestationObj: String,
        val clientDataJson: String,
        val originString: String,
        val rpid: String,
        val challengeString: String,
        val publicKey: String,
        val id: String,
        val personUid: Long,
        val person: Person
    )
}