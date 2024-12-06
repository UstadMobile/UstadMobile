package com.ustadmobile.lib.rest.domain.invite

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.invite.CheckContactTypeUseCase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import com.ustadmobile.lib.rest.domain.invite.sms.SendSmsUseCase
import com.ustadmobile.core.viewmodel.clazz.redeem.ClazzInviteViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.rest.domain.invite.message.SendMessageUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sqlite.SQLiteException


/**
 * UseCase to send invite link to contacts link username , email and sms , contacts receive as link then these contact are
 * are checked by CheckContactTypeUseCase to find valid contacts , then it store in clazzinvite db and each contact sent to particular
 * contact channel
 */

class ProcessInviteUseCase(
    private val sendEmailUseCase: SendEmailUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val checkContactTypeUseCase: CheckContactTypeUseCase,
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
    private val repo: UmAppDatabase?,
) {
    data class InviteResult(
        val inviteSent: String
    )

    suspend operator fun invoke(
        contacts: List<String>,
        clazzUid: Long,
        role: Long,
        personUid: Long
    ): InviteResult {
        try {
            val effectiveDb = repo ?: db
            val invites = contacts.map { contact ->
                val token = uuid4().toString()
                val validContact = checkContactTypeUseCase.invoke(contact = contact)

                if (validContact != null && validContact.isValid) {
                    ClazzInvite(
                        ciPersonUid = personUid,
                        ciRoleId = role,
                        ciClazzUid = clazzUid,
                        inviteType = validContact.inviteType,
                        inviteToken = token,
                        inviteContact = validContact.text
                    )
                } else {
                    null
                }
            }.filterNotNull()
            /**
             * saving contact to clazzinvite
             */
            if (invites.isNotEmpty()) {

                effectiveDb.withDoorTransactionAsync {
                    effectiveDb.clazzInviteDao().insertAll(invites)
                }

                invites.forEach { invite ->
                    val inviteLink =
                        UstadUrlComponents(endpoint.url, ClazzInviteViewModel.DEST_NAME, "inviteCode=${invite.inviteToken}").fullUrl()

                    when (invite.inviteType) {
                        1 -> {
                            invite.inviteContact?.let { sendEmailUseCase.invoke(it, inviteLink) }
                        }

                        2 -> {
                            invite.inviteContact?.let { sendSmsUseCase.invoke(it, inviteLink) }
                        }

                        3 -> {
                            invite.inviteContact?.let { sendMessageUseCase.invoke(it, inviteLink, personUid) }
                        }
                    }
                }
            } else {
                Napier.d { "ProcessInviteUseCase: No valid invites to send" }
            }

        } catch (e: Exception) {
            Napier.e(e) { "ProcessInviteUseCase: ${e.message}" }
            InviteResult("invitation error: ${e.message}")
        }
        return InviteResult("invitation sent")

    }
}
