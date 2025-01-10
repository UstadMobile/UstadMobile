package com.ustadmobile.lib.rest.domain.invite.message

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Message
import io.github.aakira.napier.Napier

class SendMessageUseCase(
    private val activeDb: UmAppDatabase,

    ) {
    suspend operator fun invoke(
        clazzName:String,
        username: String,
        link: String,
        personUid: Long
    ) {
        try {
            val person = activeDb.personDao().findByUsername(username.drop(1))
            person?.let {
                activeDb.messageDao().insert(
                    Message(
                        messageSenderPersonUid = personUid,
                        messageText = "Invitation to $clazzName $link",
                        messageToPersonUid = it.personUid,
                        messageTimestamp = systemTimeInMillis(),
                    )
                )
            }?:  {
                Napier.e { "SendMessageUseCase $username not found " }
            }
        }catch (e:Exception){
            Napier.d { "SendMessageUseCase ${e.message} " }
        }

    }
}