package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.UmState
import react.ComponentType
import react.RBuilder
import react.RComponent
import react.ReactElement
import styled.StyledHandler
import styled.StyledProps

@JsModule("react-beautiful-dnd")
@JsNonModule
external val dndModule: dynamic

external interface DragDropContextProps: StyledProps {
    var onDragStart: () -> Unit
    var onDragUpdate: () -> Unit
    var onDragEnd: () -> Unit
}


external interface DroppableProps: StyledProps {
    var droppableId: dynamic
}

external interface DraggableProps: StyledProps {
    var draggableId: dynamic
}

@Suppress("UnsafeCastFromDynamic")
private val dndDragDropContextComponent: RComponent<DragDropContextProps, UmState> = dndModule.DragDropContext

@Suppress("UnsafeCastFromDynamic")
private val dndDroppableComponent: RComponent<DroppableProps, UmState> = dndModule.Droppable

@Suppress("UnsafeCastFromDynamic")
private val dndDraggableComponent: RComponent<DroppableProps, UmState> = dndModule.Draggable

private fun RBuilder.draggableContext(
    onDragEnd: () -> Unit,
    className: String? = null,
    handler: StyledHandler<DragDropContextProps>? = null
) = createStyledComponent(dndDragDropContextComponent.unsafeCast<ComponentType<DragDropContextProps>>(), className, handler) {
    attrs.onDragEnd = onDragEnd
}


private fun RBuilder.droppable(
    droppableId: String = "droppable-list",
    className: String? = null,
    handler: StyledHandler<DroppableProps>? = null
) = createStyledComponent(dndDroppableComponent.unsafeCast<ComponentType<DroppableProps>>(), className, handler) {
    attrs.droppableId = droppableId
}

fun RBuilder.draggable(
    draggableId: dynamic,
    className: String? = null,
    handler: StyledHandler<DraggableProps>? = null
) = createStyledComponent(dndDraggableComponent.unsafeCast<ComponentType<DraggableProps>>(), className, handler) {
    attrs.draggableId = draggableId
}

fun RBuilder.draggableList(
    onDragEnd: () -> Unit,
    children: () -> ReactElement,
){
    draggableContext(onDragEnd){
        droppable{
            children()
        }
    }
}




