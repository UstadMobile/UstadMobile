package com.ustadmobile.core.domain.clipboard

interface SetClipboardStringUseCase {

    operator fun invoke(content: String)

}