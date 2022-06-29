package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry

interface PDFContentView : UstadView {

    var entry: ContentEntry?

    var filePath: String?

    companion object {

        const val VIEW_NAME = "PDFContentView"
    }
}
