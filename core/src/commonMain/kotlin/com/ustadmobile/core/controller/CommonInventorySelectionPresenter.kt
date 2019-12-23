package com.ustadmobile.core.controller

import com.ustadmobile.core.view.UstadView

/**
 * So that we can add click listener for different use cases.
 * @param <V>   The view
</V> */
abstract class CommonInventorySelectionPresenter<V : UstadView> : UstadBaseController<V> {


    var inventorySelection = false

    //The constructor will throw an uncast check warning. That is expected.
    constructor(context: Any, arguments: Map<String, String>?, view: UstadView)
            : super(context, arguments!!, view as V) {}


    /**
     * Secondary action on item.
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    abstract fun updateWeCount(weUid: Long, count: Int, saleItemUid: Long)
}