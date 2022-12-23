
@file:JsModule("react-easy-sort")
@file:JsNonModule

/*
 * Kotlin-JS wrapper for React Easy Sort https://github.com/ValentinH/react-easy-sort
 */
package com.ustadmobile.wrappers.reacteasysort

import dom.html.HTMLElement
import react.*

external interface SortableListProps: PropsWithChildren {

    override var children: ReactNode?

    var allowDrag: Boolean?

    var onSortEnd: (oldIndex: Int, newIndex: Int) -> Unit

    var draggedItemClassName: String?

    var `as`: IntrinsicType<*>?

    var lockAxis: LockAxis?

    @Suppress("unused")
    var customHolderRef: RefObject<HTMLElement>?

}

@JsName("default")
external val SortableList: FC<SortableListProps>

/**
 * As per react-easy-sort documentation:
 * This component doesn't take any other props than its child. This child should be a single React
 * element that can receives a ref. If you pass a component as a child, it needs to be wrapped with
 * React.forwardRef().
 * See: https://github.com/ValentinH/react-easy-sort#sortableitem
 */
external interface ItemProps : Props{
    var children: ReactNode?
}

/**
 * As per the ItemProps documentation, the children element should be a single React Node that can
 * receive a ref. Use it like this:
 *
 * SortableItem {
 *    children = div.create {
 *       //Content here
 *    }
 * }
 */
external val SortableItem: FC<ItemProps>

/**
 * Optional, You can use this component if you don't want the whole item to be draggable but only a
 * specific area of it.
 */
@Suppress("unused")
external val SortableKnob: FC<ItemProps>


