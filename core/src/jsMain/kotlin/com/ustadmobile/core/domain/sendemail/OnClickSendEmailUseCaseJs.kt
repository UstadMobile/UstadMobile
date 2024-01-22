package com.ustadmobile.core.domain.sendemail

import web.window.WindowTarget
import web.window.window

class OnClickSendEmailUseCaseJs: OnClickEmailUseCase {
    override fun invoke(emailAddr: String) {
        window.open("mailto:$emailAddr", WindowTarget._blank)
    }
}