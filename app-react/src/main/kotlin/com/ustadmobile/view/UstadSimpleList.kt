package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.StyleManager.listComponentContainerWithScroll
import com.ustadmobile.util.StyleManager.listCreateNewContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.renderCreateNewItemOnList
import kotlinx.css.*
import mui.material.ListItemAlignItems
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactElement
import styled.css
import styled.styledDiv

interface SimpleListProps<T>: UmProps {
    var entries: List<T>
    var onEntryClicked: ((entry: dynamic) -> Unit)?
    var createNewItem: CreateNewItem?

    /**
     * Flag to indicate whether the list items can be dragged to re-order
     */
    var draggable: Boolean

    /**
     * TRUE if this will be used as a main content of a component
     * otherwise it will be inner content.
     * i.e TRUE = Component has this list as only content
     *     FALSE = Component has other contents and this list included
     */
    var mainList: Boolean

    var presenter: UstadBaseController<*>?

    var listener: OneToManyJoinEditListener<T>?

    var hideDivider: Boolean

    var onSortEnd: ((Int, Int) -> Unit)?
}

data class CreateNewItem(var visible: Boolean = false, var text: String = "", var onClickCreateNew: (() -> Unit)? = null)

/**
 * This is meant for simple lists which on Android are either paged or simple list.
 */
abstract class UstadSimpleList<P: SimpleListProps<*>>(mProps: P) : UstadBaseComponent<P,UmState>(mProps){

    override fun RBuilder.render() {
        styledDiv {
            css{
                if(!props.mainList){
                    +listComponentContainer
                    width = 98.pct
                } else {
                    +listComponentContainerWithScroll
                }
            }
            renderMoreDialogOptions()
            renderList()
        }
    }

    open fun RBuilder.renderMoreDialogOptions(){}

    private fun RBuilder.renderItems(): ReactElement<UmProps>? {
        if(props.createNewItem?.visible == true && !props.createNewItem?.text.isNullOrEmpty()){
            umListItem(button = true, alignItems = ListItemAlignItems.flexStart) {
                css{
                    +listCreateNewContainer
                    marginBottom = 1.spacingUnits
                }
                attrs.onClick = {
                    Util.stopEventPropagation(it)
                    props.createNewItem?.onClickCreateNew?.invoke()
                }
                renderCreateNewItemOnList(props.createNewItem?.text ?: "")
            }
        }

        props.entries.forEachIndexed{index, entry ->
            if(props.draggable){
                umSortableItem("key_$index"){
                    umListItem(button = true, alignItems = ListItemAlignItems.flexStart) {
                        css{
                            backgroundColor = Color(StyleManager.theme.palette.background.paper)
                            width = 100.pct
                        }

                        attrs.divider = true
                        renderListItem(entry){
                            it.stopPropagation()
                            props.onEntryClicked?.invoke(entry)
                        }
                    }
                }
            }else {
                umListItem(button = true, alignItems = ListItemAlignItems.flexStart) {
                    css{
                        backgroundColor = Color(StyleManager.theme.palette.background.paper)
                        width = 100.pct
                    }

                    attrs.divider = true
                    renderListItem(entry){
                        it.stopPropagation()
                        props.onEntryClicked?.invoke(entry)
                    }
                }
            }
        }
        return null
    }

    private fun RBuilder.renderList(){
        if(!props.draggable){
            umList {
                css(horizontalList)
                renderItems()
            }
        }else {
            umSortableList(onSortEnd = props.onSortEnd) {
                css(horizontalList)
                renderItems()
            }
        }
    }

    abstract fun RBuilder.renderListItem(item: dynamic, onClick: (Event)-> Unit)
}