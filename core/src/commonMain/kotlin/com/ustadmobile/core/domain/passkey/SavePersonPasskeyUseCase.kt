package com.ustadmobile.core.domain.passkey

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.PersonPasskey


class SavePersonPasskeyUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?
) {

    suspend operator fun invoke(
        passkeyData: PasskeyData,
    ): Long {
        val effectiveDb = (repo ?: db)
        return effectiveDb.withDoorTransactionAsync {

            val personPasskey = passkeyData.personUid?.let { personUid ->
                PersonPasskey(
                    personUid = personUid,
                    attestationObj = passkeyData.attestationObj,
                    clientDataJson = passkeyData.clientDataJson,
                    originString = passkeyData.originString,
                    rpid = passkeyData.rpid,
                    id = passkeyData.id,
                    challengeString = passkeyData.challengeString,
                    publicKey = passkeyData.publicKey
                )
            }
            personPasskey?.let { it1 -> effectiveDb.personPasskeyDao().insertAsync(it1) }


        } ?:0L
    }

}