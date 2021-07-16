package com.ustadmobile.view

import com.ustadmobile.util.PaginateOnScrollManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.iframeComponentResponsiveIframe
import kotlinx.browser.document
import kotlinx.html.id
import kotlinx.html.js.onLoadFunction
import react.*
import react.dom.attrs
import styled.css
import styled.styledDiv
import styled.styledIframe

interface IframeProps: RProps {
    var sources: List<String>?
    var pageSize: Int
}


class  IframeComponent(mProps: IframeProps): RComponent<IframeProps,RState>(mProps){

    private var sourceToLoad: HashSet<String> = hashSetOf()

    private var didUpdate = false

    private var paginateOnScrollManager: PaginateOnScrollManager? = null

    //iframeHeight is used by js code
    @Suppress("UNUSED_VARIABLE")
    override fun RBuilder.render() {
       styledDiv{
           css(contentContainer)
           sourceToLoad.forEach {
               val iframeId = js("it.split('/').pop().split('#')[0].split('?')[0];").toString()
               styledIframe{
                   css(iframeComponentResponsiveIframe)
                   attrs {
                       src = it
                       id = iframeId
                       onLoadFunction = { loadEvent ->
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

    override fun componentDidUpdate(prevProps: IframeProps, prevState: RState, snapshot: Any) {
        if(!didUpdate && prevProps.sources?.isNotEmpty() == true){
            didUpdate = true
            val sources = props.sources
            paginateOnScrollManager = PaginateOnScrollManager(prevProps.sources?.size!!, prevProps.pageSize)
            paginateOnScrollManager?.onScrollPageChanged = { _, _, endIndex ->
                if(sources != null){
                    val items = sources.slice(IntRange(0, endIndex)).toMutableList()
                    setState {
                        sourceToLoad = items.toHashSet()
                    }
                }
            }
        }
    }

    override fun componentWillUnmount() {
        paginateOnScrollManager?.onDestroy()
    }

}

fun RBuilder.renderIframe(urls: List<String>?, pageSize: Int = 10) = child(IframeComponent::class) {
    attrs.pageSize = pageSize
    attrs.sources = urls
}