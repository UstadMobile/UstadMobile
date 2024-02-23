package com.ustadmobile.core.util.ext

import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import web.window.WindowTarget

fun OpenExternalLinkUseCase.Companion.LinkTarget.asWindowTarget(): WindowTarget {
    return when(this) {
        OpenExternalLinkUseCase.Companion.LinkTarget.BLANK -> WindowTarget._blank
        OpenExternalLinkUseCase.Companion.LinkTarget.TOP -> WindowTarget._top
        OpenExternalLinkUseCase.Companion.LinkTarget.SELF -> WindowTarget._self
        OpenExternalLinkUseCase.Companion.LinkTarget.DEFAULT -> WindowTarget._blank
    }
}