package com.ustadmobile.core.domain.openlink

import java.awt.Desktop
import java.net.URI

class OpenExternalLinkUseCaseJvm(): OpenExternalLinkUseCase {
    override fun invoke(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }
}
