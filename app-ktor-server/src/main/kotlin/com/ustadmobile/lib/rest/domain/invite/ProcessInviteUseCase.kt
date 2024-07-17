package com.ustadmobile.lib.rest.domain.invite

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.invite.CheckContactTypeUseCase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import com.ustadmobile.lib.rest.domain.invite.sms.SendSmsUseCase
import com.ustadmobile.core.viewmodel.clazz.redeem.ClazzInviteViewModel
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.rest.domain.invite.message.SendMessageUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProcessInviteUseCase(
    private val sendEmailUseCase: SendEmailUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val checkContactTypeUseCase: CheckContactTypeUseCase,
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
    private val  repo: UmAppDatabase?,
) {
    data class InviteResult(
        val inviteSent: String
    )

    operator fun invoke(
        contacts: List<String>,
        clazzUid: Long,
        role: Long,
        personUid: Long
    ): InviteResult {
        try {
            val effectiveDb = (repo ?: db)

            val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
            coroutineScope.launch {

                contacts.forEach { contact ->
                    val token = uuid4().toString()
                    val inviteLink = UstadUrlComponents(
                        endpoint.url, ClazzInviteViewModel.DEST_NAME, "inviteCode=$token"
                    ).fullUrl()
                    val validContacts = checkContactTypeUseCase.invoke(contact = contact)


                    if (validContacts != null) {
                        val log = effectiveDb.clazzInviteDao().replace(
                            ClazzInvite(
                                ciPersonUid = personUid,
                                ciRoleId = role,
                                ciClazzUid = clazzUid,
                                inviteType = validContacts.inviteType,
                                inviteToken = token,
                                inviteContact = validContacts.text
                            )
                        )

                        Napier.d { "ProcessInviteUseCase $log" }


                        when (validContacts.inviteType) {
                            1 -> {
                                sendEmailUseCase.invoke(validContacts.text, inviteLink)
                            }

                            2 -> {
                                sendSmsUseCase.invoke(validContacts.text, inviteLink)
                            }

                            3 -> {
                                sendMessageUseCase.invoke(validContacts.text, inviteLink, personUid)
                            }

                        }
                    }
                }
            }

            return InviteResult("invitation sent")

        } catch (e: Exception) {
            Napier.d { "ProcessInviteUseCase ${e.message}" }
            return InviteResult("invitation error :- ${e.message}")
        }

    }
}

