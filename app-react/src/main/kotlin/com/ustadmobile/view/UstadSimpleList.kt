package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.mui.components.ListItemAlignItems
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umList
import com.ustadmobile.mui.components.umListItem
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.StyleManager.listComponentContainerWithScroll
import com.ustadmobile.util.StyleManager.listCreateNewContainer
import com.ustadmobile.util.StyleManager.theme
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.createCreateNewItem
import kotlinx.css.*
import org.w3c.dom.events.Event
import react.RBuilder
import styled.css
import styled.styledDiv

interface SimpleListProps<T>: UmProps {
    var entries: List<T>
    var onEntryClicked: ((entry: dynamic) -> Unit)?
    var createNewItem: CreateNewItem?

    /**
     * TRUE if this will be used as a main content of a component
     * otherwise it will be inner content.
     * i.e TRUE = Component has this list as only content
     *     FALSE = Component has other contents and this list included
     */
    var mainList: Boolean

    var presenter: UstadBaseController<*>

    var listener: OneToManyJoinEditListener<T>?

    var hideDivider: Boolean
}

data class CreateNewItem(var visible: Boolean = false, var labelId: Int = 0, var onClickCreateNew: (() -> Unit)? = null)

/**
 * This is meant for simple lists which on Android are either paged or simple list.
 */
abstract class UstadSimpleList<P: SimpleListProps<*>>(mProps: P) : UstadBaseComponent<P,UmState>(mProps){

    override val viewNames: List<String>? = null

    override fun RBuilder.render() {
        styledDiv {
            css{
                if(!props.mainList){
                    +listComponentContainer
                    width = LinearDimension("98%")
                } else {
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
                    css{
                        +listCreateNewContainer
                        marginBottom = 1.spacingUnits
                    }
                    attrs.onClick = {
                        Util.stopEventPropagation(it)
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

                    attrs.divider = true
                    renderListItem(entry){
                        it.stopPropagation()
                        props.onEntryClicked?.invoke(entry)
                    }
                }
            }
        }
    }

    abstract fun RBuilder.renderListItem(item: dynamic, onClick: (Event)-> Unit)
}