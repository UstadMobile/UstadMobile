package com.ustadmobile.core.view

interface UstadBaseFeedbackMessageView : UstadView {

    fun showFeedbackMessage(message: String, actionMessageId: Int = 0, action: () -> Unit = {})

}
