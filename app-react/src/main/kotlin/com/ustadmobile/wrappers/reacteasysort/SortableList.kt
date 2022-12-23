
@file:JsModule("react-easy-sort")
@file:JsNonModule

package com.ustadmobile.wrappers.reacteasysort

import react.Props
import react.PropsWithChildren
import react.ReactNode

external interface SortableListProps: PropsWithChildren {

    var onSortEnd: (oldIndex: Int, newIndex: Int) -> Unit

    var draggedItemClassName: String?

    var lockAxis: String?

    var allowDrag: Boolean

}

@JsName("default")
external val SortableList: react.FC<SortableListProps>

external interface ItemProps : Props{
    var children: ReactNode?
}

external val SortableItem: react.FC<ItemProps>

external val SortableKnob: react.FC<ItemProps>


