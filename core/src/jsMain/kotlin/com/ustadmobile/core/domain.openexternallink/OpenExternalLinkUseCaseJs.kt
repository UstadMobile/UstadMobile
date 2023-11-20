package com.ustadmobile.core.domain.openexternallink

import web.window.WindowTarget
import web.window.window

class OpenExternalLinkUseCaseJs : OpenExternalLinkUseCase{

    override fun invoke(url: String) {
        window.open(url, WindowTarget._blank)
    }

}