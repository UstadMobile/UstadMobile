package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import com.ustadmobile.util.UmState
import react.ComponentType
import react.RBuilder
import react.RComponent
import styled.StyledHandler
import styled.StyledProps


@JsModule("react-easy-sort")
@JsNonModule
external val sortModule: dynamic

external interface SortableListProps: StyledProps{
    var onSortEnd: (dynamic, dynamic) -> Unit
    var allowDrag: Boolean
}

external interface SortableItemProps: StyledProps {
    override var key: String?
}

external interface  SortableKnobProps: StyledProps

@Suppress("UnsafeCastFromDynamic")
private val SortableListComponent: RComponent<SortableListProps, UmState> = sortModule.default


@Suppress("UnsafeCastFromDynamic")
private val SortableItemComponent: RComponent< SortableItemProps, UmState> = sortModule.SortableItem

@Suppress("UnsafeCastFromDynamic")
private val SortableHandleComponent: RComponent< SortableKnobProps, UmState> = sortModule.SortableKnob

fun RBuilder.umSortableList(
    onSortEnd: ((Int, Int) -> Unit)?,
    useDragHandle: Boolean = true,
    className: String? = null,
    handler: StyledHandler<SortableListProps>? = null
) = convertFunctionalToClassElement(
    SortableListComponent.unsafeCast<ComponentType<SortableListProps>>(),
    className, handler){
    onSortEnd?.let {
        attrs.onSortEnd = it
    }
    attrs.allowDrag = useDragHandle
    this.create()
}

fun RBuilder.umSortableItem(
    key: String,
    className: String? = null,
    handler: StyledHandler<SortableItemProps>? = null
) = convertFunctionalToClassElement(
    SortableItemComponent.unsafeCast<ComponentType<SortableItemProps>>(),
    className, handler){
    attrs.key = key
}

fun RBuilder.umSortableKnob(
    className: String? = null,
    handler: StyledHandler<SortableKnobProps>? = null) = convertFunctionalToClassElement(
    SortableHandleComponent.unsafeCast<ComponentType<SortableKnobProps>>(),
    className, handler)


