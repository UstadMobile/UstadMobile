package com.ustadmobile.view

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.controller.EpubContentPresenter
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.util.CssStyleManager.responsiveIframe
import com.ustadmobile.util.RouteManager.getArgs
import kotlinx.browser.window
import kotlinx.html.IframeSandbox
import kotlinx.html.js.onLoadFunction
import react.*
import styled.css
import styled.styledIframe

class EpubContentComponent(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), EpubContentView{

    private val pageList = mutableListOf<ReactElement>()

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
        }

    override var spineUrls: List<String>? = null
        get() = field
        set(value) {
            loading = value == null
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
            loading = value
            field = value
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
        if(spineUrls != null){
            spineUrls?.forEach {
                styledIframe{
                    css{ +responsiveIframe }
                    attrs{
                        sandbox = IframeSandbox.allowSameOrigin
                        src = it
                        onLoadFunction = {}
                    }
                }
            }
        }
    }
}