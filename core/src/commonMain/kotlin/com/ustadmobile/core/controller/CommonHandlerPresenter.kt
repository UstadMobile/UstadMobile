package com.ustadmobile.core.controller

import com.ustadmobile.core.view.UstadView

/**
 * So that we can add click listener for different use cases.
 * @param <V>   The view
</V> */
abstract class CommonHandlerPresenter<V : UstadView> : UstadBaseController<V> {

    //The constructor will throw an uncast check warning. That is expected.
    constructor(context: Any, arguments: Map<String, String>?, view: UstadView)
            : super(context, arguments!!, view as V) {}

    /**
     * Primary action on item.
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    abstract fun handleCommonPressed(arg: Any)

    /**
     * Secondary action on item.
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    abstract fun handleSecondaryPressed(arg: Any)
}