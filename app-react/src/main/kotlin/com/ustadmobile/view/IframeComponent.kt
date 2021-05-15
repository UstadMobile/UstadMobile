package com.ustadmobile.view

import com.ustadmobile.core.view.UstadView.Companion.KEY_IFRAME_HEIGHTS
import com.ustadmobile.util.CssStyleManager.responsiveIframe
import com.ustadmobile.util.PaginateOnScrollManager
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.id
import kotlinx.html.js.onLoadFunction
import react.*
import styled.css
import styled.styledIframe

interface IframeProps: RProps {

    var sources: List<String>?

    var pageSize: Int
}


class  IframeComponent(mProps: IframeProps): RComponent<IframeProps,RState>(mProps){

    private var sourceToLoad: HashSet<String> = hashSetOf()

    private var didUpdate = false

    private var paginateOnScrollManager: PaginateOnScrollManager? = null

    override fun RBuilder.render() {
        sourceToLoad.forEach {
            val iframeId = js("it.split('/').pop().split('#')[0].split('?')[0];").toString()
            styledIframe{
                css{ +responsiveIframe }
                attrs{
                    src = it
                    id = iframeId
                    onLoadFunction = {
                        window.setTimeout({
                            val storageKey = KEY_IFRAME_HEIGHTS
                            val heightsMap = js("var stringMap = localStorage.getItem(storageKey); stringMap ? JSON.parse(stringMap): {}")
                            val iframeElement = document.getElementById(iframeId)
                            val iframeHeight = iframeElement?.getBoundingClientRect()?.height?.toInt()?:600
                            var contentHeight = heightsMap[iframeId]
                            val computedHeight = js("contentHeight?contentHeight:iframeHeight").toString()
                            iframeElement.asDynamic().style.height = "${computedHeight}px"
                        },200)
                    }
                }
            }
        }
    }

    override fun componentDidUpdate(prevProps: IframeProps, prevState: RState, snapshot: Any) {
        if(!didUpdate && prevProps.sources?.isNotEmpty() == true){
            didUpdate = true
            val sources = props.sources
            paginateOnScrollManager = PaginateOnScrollManager(prevProps.sources?.size!!, 10)
            paginateOnScrollManager?.onPageChanged = {_, endIndex ->
                if(sources != null){
                    val items = sources.slice(IntRange(0, endIndex)).toMutableList()
                    setState { sourceToLoad = items.toHashSet() }
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