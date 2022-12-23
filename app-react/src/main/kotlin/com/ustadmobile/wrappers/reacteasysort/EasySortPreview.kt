package com.ustadmobile.wrappers.reacteasysort

import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.useState

val EasySortPreview = FC<Props> {

    var itemList: List<String> by useState { listOf("One", "Two", "Three") }

    SortableList {
        `as` = div
        allowDrag = true
        draggedItemClassName = "dragged"
        lockAxis = LockAxis.y

        onSortEnd = { oldIndex, newIndex ->
            itemList = itemList.toMutableList().apply {
                add(newIndex, removeAt(oldIndex))
            }.toList()
        }

        itemList.forEach { itemStr ->
            SortableItem {
                children = div.create {
                    +itemStr
                }
            }
        }
    }
}