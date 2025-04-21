package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.PersonPasskey
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase.CreatePasskeyResult


class SavePersonPasskeyUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?
) {
    suspend operator fun invoke(
        passkeyResult: CreatePasskeyResult,
    ): Long {
        val effectiveDb = (repo ?: db)

        val personPasskey = PersonPasskey(
            ppPersonUid = passkeyResult.personUid,
            ppAttestationObj = passkeyResult.attestationObj,
            ppClientDataJson = passkeyResult.clientDataJson,
            ppOriginString = passkeyResult.originString,
            ppRpid = passkeyResult.rpid,
            ppId = passkeyResult.id,
            ppChallengeString = passkeyResult.challengeString,
            ppPublicKey = passkeyResult.publicKey
        )

        return effectiveDb.personPasskeyDao().insertAsync(personPasskey)


    }


}