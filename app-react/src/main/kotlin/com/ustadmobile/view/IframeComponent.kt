package com.ustadmobile.view

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

interface IframeProps: UmProps {
    var sources: List<String>
    var pageSize: Int
    var contentTypeEpub: Boolean
}


class  IframeComponent(mProps: IframeProps): RComponent<IframeProps, UmState>(mProps){

    private var sourcesUrlToLoad: List<String> = listOf()

    private var paginateOnScrollManager: PaginateOnScrollManager? = null

    //iframeHeight is used by js code
    @Suppress("UNUSED_VARIABLE")
    override fun RBuilder.render() {
       styledDiv{
           css(contentContainer)
           sourcesUrlToLoad.forEach {
               val iframeId = js("it.split('/').pop().split('#')[0].split('?')[0];").toString()
               styledIframe{
                   css(StyleManager.iframeComponentResponsiveIframe)
                   attrs {
                       src = it+if(props.contentTypeEpub) "?contentTypeEpub=${props.contentTypeEpub}" else ""
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

    override fun componentDidUpdate(prevProps: IframeProps, prevState: UmState, snapshot: Any) {
        if(prevProps.sources.isNotEmpty() &&  !sourcesUrlToLoad.containsAll(prevProps.sources)){
            paginateOnScrollManager = PaginateOnScrollManager(prevProps.sources.size, prevProps.pageSize)
            paginateOnScrollManager?.onScrollPageChanged = { _, _, endIndex -> run{
                sourcesUrlToLoad = prevProps.sources.slice(IntRange(0, endIndex))
                }
            }
        }
    }

    override fun componentWillUnmount() {
        paginateOnScrollManager?.onDestroy()
    }

}

fun RBuilder.renderIframe(
    urls: List<String>,
    pageSize: Int = 10,
    epubType: Boolean = false
) = child(IframeComponent::class) {
    attrs.pageSize = pageSize
    attrs.sources = urls.distinct()
    attrs.contentTypeEpub = epubType
}