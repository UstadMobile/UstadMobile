package com.ustadmobile.view

import com.ccfraser.muirwik.components.list.MListItemAlignItems
import com.ccfraser.muirwik.components.list.alignItems
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItem
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.innerContentContainer
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.StyleManager.theme
import kotlinx.css.Color
import kotlinx.css.LinearDimension
import kotlinx.css.backgroundColor
import kotlinx.css.width
import react.RBuilder
import react.RProps
import react.RState
import styled.css
import styled.styledDiv

interface ListProps<T>: RProps {
    var entries: List<T>
}

abstract class UstadList<T>(mProps: ListProps<T>) : UstadBaseComponent<ListProps<T>,RState>(mProps){

    var list: List<T>? = null

    override val viewName: String?
        get() = ""

    override fun RBuilder.render() {
        styledDiv {
            css{
                +listComponentContainer
                +innerContentContainer
                +defaultDoubleMarginTop
            }
            renderList()
        }
    }

    private fun RBuilder.renderList(){
        mList {
            css(horizontalList)

            list?.forEach {entry->
                mListItem {
                    css{
                        backgroundColor = Color(theme.palette.background.paper)
                        width = LinearDimension("100%")
                    }
                    attrs.alignItems = MListItemAlignItems.flexStart
                    attrs.button = true
                    attrs.divider = true
                    attrs.onClick = {
                        handleClickEntry(entry)
                    }
                    renderListItem(entry)
                }
            }
        }
    }

    abstract fun RBuilder.renderListItem(item: T)

    fun handleClickEntry(entry: T){}
}