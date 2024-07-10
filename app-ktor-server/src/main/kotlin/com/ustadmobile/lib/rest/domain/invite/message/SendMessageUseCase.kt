package com.ustadmobile.lib.rest.domain.invite.message

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Message
import io.github.aakira.napier.Napier

class SendMessageUseCase(
    private val activeDb: UmAppDatabase,

    ) {
    suspend operator fun invoke(username: String, link: String, personUid: Long) {
        try {
            val person = activeDb.personDao.findByUsername(username)
            person?.let {
                activeDb.messageDao.insert(
                    Message(
                        messageSenderPersonUid = personUid,
                        messageText = link,
                        messageToPersonUid = it.personUid,
                        messageTimestamp = systemTimeInMillis(),
                    )
                )
            }
        }catch (e:Exception){
            Napier.d { "SendMessageUseCase ${e.message} " }
        }

    }
}