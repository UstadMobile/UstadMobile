package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.MessageRead

interface MessagesPresenter {

    fun updateMessageRead(messageRead: MessageRead)

}