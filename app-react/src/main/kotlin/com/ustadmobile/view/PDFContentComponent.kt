package com.ustadmobile.view

import com.ustadmobile.core.controller.PDFContentPresenter
import com.ustadmobile.core.view.PDFContentView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import react.RBuilder
import react.setState

class PDFContentComponent(mProps: UmProps):UstadBaseComponent<UmProps, UmState>(mProps),
    PDFContentView {

    override var entry: ContentEntry? = null
        set(value) {
            field = value
            ustadComponentTitle = value?.title
            mPresenter?.onResume()
        }

    override var pdfContainerUid: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    private var mPresenter: PDFContentPresenter? = null

    private var containerUid: Long = 0

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        containerUid = arguments[ARG_CONTAINER_UID]?.toLong() ?: 0L
        mPresenter = PDFContentPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        if(pdfContainerUid != 0L){
            renderPDFIframe(listOf("/pdf/" +pdfContainerUid), epubType = false)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entry = null
    }
}