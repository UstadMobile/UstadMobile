package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.mui.components.ListItemAlignItems
import com.ustadmobile.mui.components.umList
import com.ustadmobile.mui.components.umListItem
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.StyleManager.listComponentContainerWithScroll
import com.ustadmobile.util.*
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.theme
import com.ustadmobile.view.ext.createCreateNewItem
import kotlinx.css.Color
import kotlinx.css.LinearDimension
import kotlinx.css.backgroundColor
import kotlinx.css.width
import react.RBuilder
import styled.css
import styled.styledDiv

interface ListProps<T>: UmProps {
    var entries: List<T>
    var onEntryClicked: ((entry: dynamic) -> Unit)?
    var createNewItem: CreateNewItem?

    /**
     * TRUE if this will be used as a main content of a component
     * otherwise it will be inner content.
     * i.e TRUE = Component has this list as only content
     *     FALSE = Component has other contents and this list include
     */
    var mainList: Boolean

    var presenter: UstadBaseController<*>

    var listener: OneToManyJoinEditListener<T>
}

data class CreateNewItem(var visible: Boolean = false, var labelId: Int = 0, var onClickCreateNew: (() -> Unit)? = null)

abstract class UstadSimpleList<P: ListProps<*>>(mProps: P) : UstadBaseComponent<P,UmState>(mProps){

    override val viewName: String?
        get() = ""

    override fun RBuilder.render() {
        styledDiv {
            css{
                if(!props.mainList){
                    +listComponentContainer
                    width = LinearDimension("98%")
                }else{
                    +listComponentContainerWithScroll
                }
            }
            renderList()
        }
    }

    private fun RBuilder.renderList(){
        umList {
            css(horizontalList)

            if(props.createNewItem?.visible == true && props.createNewItem?.labelId != 0){
                umListItem(button = true, alignItems = ListItemAlignItems.flexStart) {
                    css(StyleManager.listCreateNewContainer)
                    attrs.divider = true
                    attrs.onClick = {
                        props.createNewItem?.onClickCreateNew?.invoke()
                    }
                    createCreateNewItem(getString(props.createNewItem?.labelId ?: 0))
                }
            }

            props.entries.forEach {entry->
                umListItem(button = true, alignItems = ListItemAlignItems.flexStart) {
                    css{
                        backgroundColor = Color(theme.palette.background.paper)
                        width = LinearDimension("100%")
                    }
                    attrs.onClick = {
                        props.onEntryClicked?.invoke(entry)
                    }
                    attrs.divider = true
                    renderListItem(entry)
                }
            }
        }
    }

    abstract fun RBuilder.renderListItem(item: dynamic)
}