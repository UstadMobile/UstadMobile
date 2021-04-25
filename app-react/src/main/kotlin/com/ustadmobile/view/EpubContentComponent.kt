package com.ustadmobile.view

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.util.RouteManager.getArgs
import kotlinx.css.LinearDimension
import react.RBuilder
import react.RProps
import react.RState
import react.dom.iframe
import react.setState
import styled.styledIframe

class EpubContentComponent(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), EpubContentView{

    override var containerTitle: String? = null
        get() = field
        set(value) {
            title = value
            field = value
        }

    override var windowTitle: String? = null
        get() = field
        set(value) {
            field = value
            console.log("wT", value)
        }

    override var spineUrls: List<String>? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var tableOfContents: EpubNavItem? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var coverImageUrl: String? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var authorName: String = ""
        get() = field
        set(value) {
            setState { field = value }
        }

    override var progressVisible: Boolean = false
        get() = field
        set(value) {
            setState { field = value }
        }

    override var progressValue: Int = -1
        get() = field
        set(value) {
            setState { field = value }
        }

    private lateinit var mPresenter: EpubContentPresenter

    override fun scrollToSpinePosition(spinePosition: Int, hashAnchor: String?) {}

    override fun componentDidMount() {
        super.componentDidMount()
        mPresenter = EpubContentPresenter(this,getArgs(),this, di)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        spineUrls?.forEach {
            styledIframe {
                attrs{
                    width = "100%"
                    height = "300px"
                    src = it
                }
            }
        }
    }
}