package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry

interface PDFContentView : UstadView {

    var entry: ContentEntry?

    // The file's container uid. This is then set on the view.
    var pdfContainerUid: Long

    companion object {

        const val VIEW_NAME = "PDFContentView"
    }
}
