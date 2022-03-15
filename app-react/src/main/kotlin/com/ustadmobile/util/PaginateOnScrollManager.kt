package com.ustadmobile.util

/**
 * Handle pagination on view scrolling, this mimics an endless scrolling behaviors on iframes
 * @param totalItemCount Number of items to be paginated
 * @param pageSize size of a page
 */
class  PaginateOnScrollManager(private val totalItemCount: Int, private val pageSize: Int) {

    private var pageNumber = 1

    private var scrollManager: ScrollManager? = null

    private val totalPages:Double = totalItemCount.toDouble()/pageSize

    val startIndex: () -> Int = {
        if(((pageNumber - 1) * pageSize) > totalItemCount) 0
        else (pageNumber - 1) * pageSize
    }

    val endIndex: () -> Int = {
        if((pageNumber * pageSize) < totalItemCount) pageNumber * pageSize
        else totalItemCount - 1
    }

    //pages is used by js code
    @Suppress("UNUSED_VARIABLE")
    private var onScroll: (Boolean, Int) -> Unit = { fullFilled, _ ->
        if(fullFilled){
            var pages = totalPages
            pages = js("Math.ceil(pages)").toString().toDouble()
            if(pageNumber.toDouble() == pages) pageNumber else pageNumber++
            onScrollPageChanged?.invoke(pageNumber,startIndex(), endIndex())
        }
    }


    /**
     * On page changed takes page number as first param, startIndex and endIndex respectively
     */
    var onScrollPageChanged: ((Int, Int, Int) -> Unit?)? = null
        set(value) {
            if(value != null){
                value(pageNumber,startIndex(), endIndex())
            }
            if(pageSize != totalItemCount && totalPages > 1){
                scrollManager = ScrollManager("main-content")
                scrollManager?.scrollListener = onScroll
            }
            field = value
        }

    fun onDestroy(){
        scrollManager?.onDestroy()
        scrollManager = null
        onScrollPageChanged = null
    }

}