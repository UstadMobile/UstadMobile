package com.ustadmobile.core.controller

import com.ustadmobile.core.view.UstadView
/**
 * So that we can add click listener for different use cases.
 * @param <V>   The view
</V> */
abstract class CommonEntityHandlerPresenter<V : UstadView>//The constructor will throw an uncast check warning. That is expected.
(context: Any, arguments: Map<String, String?>, view: V) : UstadBaseController<V>(context, arguments, view) {

    abstract fun entityChecked(entityName: String, entityUid: Long, checked: Boolean)

}