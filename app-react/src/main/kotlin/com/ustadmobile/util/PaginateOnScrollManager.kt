package com.ustadmobile.util

class  PaginateOnScrollManager(private val totalItems: Int, private val pageSize: Int) {

    private var pageIndex = 1

    private var scrollManager: ScrollManager? = null

    private var onItemsScroll: (Boolean, Int) -> Unit = { fullFilled, _ ->
        if(fullFilled){
            pageIndex++
            val startIndex = if(((pageIndex - 1) * pageSize) > totalItems) 0
            else (pageIndex - 1) * pageSize
            val endIndex = if((pageIndex * pageSize) < totalItems) pageIndex * pageSize
            else totalItems - 1
            onPageChanged?.let { it(startIndex, endIndex)}
        }
    }

    var onPageChanged: ((Int, Int) -> Unit?)? = null
        set(value) {
            if(value != null){
                value(pageIndex, pageSize)
            }
            scrollManager = ScrollManager("main-content", 70)
            scrollManager?.scrollListener = onItemsScroll
            field = value
        }

    fun onDestroy(){
        scrollManager = null
        onPageChanged = null
    }

}