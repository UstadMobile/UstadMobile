package com.ustadmobile.core.domain.clipboard

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class SetClipboardStringUseCaseJvm: SetClipboardStringUseCase {

    override fun invoke(content: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selectionContent = StringSelection(content)
        clipboard.setContents(selectionContent, selectionContent)
    }

}