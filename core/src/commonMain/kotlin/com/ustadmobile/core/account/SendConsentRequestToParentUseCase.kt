package com.ustadmobile.core.account

import kotlinx.serialization.Serializable

/**
 * Interface used to Send consent request to parent via email
 */
interface SendConsentRequestToParentUseCase {

    @Serializable
    data class SendConsentRequestToParentRequest(
        val childFullName: String,
        val childDateOfBirth: Long,
        val childGender: Int,
        val parentContact: String,
        val ppjUid: Long,
        )

    suspend operator fun invoke(
        request: SendConsentRequestToParentRequest
    )

}