package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.credentials.passkey.model.ClientDataJSON
import com.ustadmobile.lib.db.entities.PersonPasskey
import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON
import com.ustadmobile.lib.db.entities.Person
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


class SavePersonPasskeyUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val json: Json
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend operator fun invoke(
        passkeyResult: AuthenticationResponseJSON,
        person: Person
    ): Long {
        val effectiveDb = (repo ?: db)

        val clientDataJSONBase64 = passkeyResult.response.clientDataJSON
        val decodedBytes = clientDataJSONBase64.decodeBase64Bytes()
        val clientDataJson = json.decodeFromString<ClientDataJSON>(decodedBytes.decodeToString())

        val personPasskey = PersonPasskey(
            ppPersonUid = person.personUid,
            ppAttestationObj = passkeyResult.response.attestationObject,
            ppClientDataJson = passkeyResult.response.clientDataJSON,
            ppOriginString = clientDataJson.origin,
            ppId = passkeyResult.id,
            ppChallengeString = clientDataJson.challenge,
            ppPublicKey = passkeyResult.response.publicKey
        )

        return effectiveDb.personPasskeyDao().insertAsync(personPasskey)


    }


}