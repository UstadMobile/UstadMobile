package com.ustadmobile.core.domain.clipboard

import web.navigator.navigator

class SetClipboardStringUseCaseJs: SetClipboardStringUseCase {

    override fun invoke(content: String) {
        try {
            navigator.clipboard.writeText(content)
        }catch(e: Throwable) {
            //Do nothing
        }
    }
}