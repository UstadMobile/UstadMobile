package com.ustadmobile.core.domain.openlink

import web.window.window
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase.Companion.LinkTarget
import com.ustadmobile.core.util.ext.asWindowTarget

class OpenExternalLinkUseCaseJs : OpenExternalLinkUseCase {

    override fun invoke(url: String, target: LinkTarget) {
        window.open(url, target.asWindowTarget())
    }

}
