package com.ustadmobile.core.domain.openlink

import java.awt.Desktop
import java.net.URI

class OpenExternalLinkUseCaseJvm(): OpenExternalLinkUseCase {
    override fun invoke(url: String, target: OpenExternalLinkUseCase.Companion.LinkTarget) {
        Desktop.getDesktop().browse(URI(url))
    }
}
