package com.ustadmobile.core.impl.locale

import android.content.Context

class AndroidStringResources(
    private val messageIdMap: Map<Int, Int>,
    private val appContext: Context
) : StringResources {

    override fun get(messageId: Int): String {
        return messageIdMap[messageId]?.let { appContext.getString(it) }
            ?: throw IllegalArgumentException("Could not find android resource id in message id map for $messageId")
    }

}