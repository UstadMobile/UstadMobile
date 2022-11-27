package com.ustadmobile.view

import com.ustadmobile.lib.util.randomString
import com.ustadmobile.util.PaginateOnScrollManager
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinx.browser.document
import kotlinx.html.id
import kotlinx.html.js.onLoadFunction
import react.RBuilder
import react.RComponent
import react.dom.attrs
import styled.css
import styled.styledDiv
import styled.styledIframe

interface PDFIframeProps: UmProps {
    var sources: List<String>
    var pageSize: Int
    var contentTypeEpub: Boolean
}


class  PDFIframeComponent(mProps: PDFIframeProps): RComponent<PDFIframeProps, UmState>(mProps){

    private var sourcesUrlToLoad: List<String> = listOf()

    private var paginateOnScrollManager: PaginateOnScrollManager? = null

    //iframeHeight is used by js code
    @Suppress("UNUSED_VARIABLE")
    override fun RBuilder.render() {
       styledDiv{
           css(contentContainer)
           sourcesUrlToLoad = props.sources
           sourcesUrlToLoad.forEach {
               val iframeId = randomString(25)
               styledIframe{
                   css(StyleManager.iframeComponentResponsiveIframe)
                   attrs {
                       src = it + if(props.contentTypeEpub) "?contentTypeEpub=${props.contentTypeEpub}" else ""
                       id = iframeId
                       onLoadFunction = { loadEvent ->
                           if(props.contentTypeEpub){
                               val contentWindow = loadEvent.target.asDynamic().contentWindow
                               val iframe = document.getElementById(iframeId)
                               val iframeDoc = js("iframe.contentDocument || iframe.contentWindow.document")
                               val scrollHeight = iframeDoc.body.scrollHeight.toString().toInt()
                               iframe.asDynamic().style.height = "${scrollHeight + (scrollHeight * 0.1)}px"
                           }
                       }
                   }
               }
           }
       }
    }

    override fun componentDidUpdate(prevProps: PDFIframeProps, prevState: UmState, snapshot: Any) {
        if(prevProps.sources.isNotEmpty() &&  !sourcesUrlToLoad.containsAll(prevProps.sources)){
            sourcesUrlToLoad = prevProps.sources

        }
    }

    override fun componentWillUnmount() {
        paginateOnScrollManager?.onDestroy()
    }

}

fun RBuilder.renderPDFIframe(
    urls: List<String>,
    pageSize: Int = 10,
    epubType: Boolean = false
) = child(PDFIframeComponent::class) {
    attrs.pageSize = pageSize
    attrs.sources = urls.distinct()
    attrs.contentTypeEpub = epubType
}