package com.ustadmobile.view

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import react.RBuilder
import react.setState

class EpubContentComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), EpubContentView{

    override var containerTitle: String? = null
        get() = field
        set(value) {
            ustadComponentTitle = value
            field = value
        }

    override var windowTitle: String? = null
        get() = field
        set(value) {
            field = value
        }

    override var spineUrls: List<String>? = null
        get() = field
        set(value) {
            loading = value == null
            setState {
                field = value
            }
        }

    override var tableOfContents: EpubNavItem? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var coverImageUrl: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var authorName: String = ""
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var progressVisible: Boolean = false
        get() = field
        set(value) {
            loading = value
            field = value
        }

    override var progressValue: Int = -1
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private var mPresenter: EpubContentPresenter? = null

    override fun scrollToSpinePosition(spinePosition: Int, hashAnchor: String?) {}

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = EpubContentPresenter(this,arguments,this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        val urls = spineUrls
        if(urls != null){
            renderIframe(urls, epubType = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        tableOfContents = null
        spineUrls = null
        windowTitle = null
        containerTitle = null
    }
}