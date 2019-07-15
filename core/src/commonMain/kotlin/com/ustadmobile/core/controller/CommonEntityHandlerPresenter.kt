package com.ustadmobile.core.controller

import com.ustadmobile.core.view.UstadView


/**
 * So that we can add click listener for different use cases.
 * @param <V>   The view
</V> */
abstract class CommonEntityHandlerPresenter<V : UstadView> : UstadBaseController<V> {

    //The constructor will throw an uncast check warning. That is expected.
    constructor(context: Any, arguments: Map<String, String>?, view: UstadView)
            : super(context, arguments!!, view as V) {}

    abstract fun entityChecked(entityName: String, entityUid: Long?, checked: Boolean)
}