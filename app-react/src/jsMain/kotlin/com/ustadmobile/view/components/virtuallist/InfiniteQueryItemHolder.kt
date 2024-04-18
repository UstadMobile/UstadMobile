package com.ustadmobile.view.components.virtuallist

import js.objects.jso
import react.FC
import react.PropsWithChildren
import react.useEffect
import tanstack.react.query.UseInfiniteQueryResult

external interface InfiniteQueryHolderProps : PropsWithChildren {
    /**
     * The UseInfiniteQueryResult
     */
    var infiniteQueryResult: UseInfiniteQueryResult<*, *>

    /**
     * The items currently loaded from the infinitequery
     */
    var loadedItems: List<*>

    /**
     * The index of the item within loadedItems
     */
    var itemIndex: Int

    /**
     * The item represented by this item
     */
    var item: Any?
}

/**
 * The InfiniteQueryItemHolder component detects when the last item in an InfiniteQueryResult is
 * being displayed in the virtual list and triggers loading the next page
 */
val InfiniteQueryItemHolder = FC<InfiniteQueryHolderProps> {props ->
    useEffect {
        if(props.item != null &&
            props.item === props.loadedItems.lastOrNull() &&
            props.infiniteQueryResult.hasNextPage &&
            !props.infiniteQueryResult.isFetchingNextPage
        ) {
            props.infiniteQueryResult.fetchNextPage(jso {  })
        }
    }

    +props.children
}
