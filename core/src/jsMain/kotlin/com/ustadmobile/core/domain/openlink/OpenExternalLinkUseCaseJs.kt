package com.ustadmobile.core.domain.openlink

import web.window.WindowTarget
import web.window.window
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase.Companion.LinkTarget

class OpenExternalLinkUseCaseJs : OpenExternalLinkUseCase {

    private fun LinkTarget.asWindowTarget(): WindowTarget {
        return when(this) {
            LinkTarget.BLANK -> WindowTarget._blank
            LinkTarget.TOP -> WindowTarget._top
            LinkTarget.SELF -> WindowTarget._self
            LinkTarget.DEFAULT -> WindowTarget._blank
        }
    }

    override fun invoke(url: String, target: LinkTarget) {
        window.open(url, target.asWindowTarget())
    }

}
