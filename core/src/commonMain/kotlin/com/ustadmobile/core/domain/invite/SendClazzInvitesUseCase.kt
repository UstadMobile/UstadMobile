package com.ustadmobile.core.domain.invite

import kotlinx.serialization.Serializable

/**
 * Interface used to Send Clazz Invites. Invites must always eventually be processed by the server
 * as this requires sending email, sms, or internal messages.
 *
 * Client implementation (mobile,web,desktop) will send an HTTP Request to the server.
 * Server backend implementation will 1) insert invites into the database and 2) send out messages
 * via email, sms, etc.
 */
interface SendClazzInvitesUseCase {

    /**
     * @param contacts list of contacts - input as separated out by ParseInviteUseCase
     * @param clazzUid the clazzuid for which person will join course
     * @param personUid the personUid making (e.g. who is sending) the invite
     * @param role the roleid to which the contacts are being invited as per ClazzEnrolment.clazzEnrolmentRole
     */
    @Serializable
    data class SendClazzInvitesRequest(
        val contacts: List<String>,
        val clazzUid: Long,
        val role: Long,
        val personUid: Long
    )

    /**
     * Send invites as per request. If not possible an exception will be thrown.
     */
    suspend operator fun invoke(
        request: SendClazzInvitesRequest
    )

}