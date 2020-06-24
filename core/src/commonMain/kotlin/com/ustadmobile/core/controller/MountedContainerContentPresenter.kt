package com.ustadmobile.core.controller

import com.ustadmobile.core.view.UstadView

abstract class MountedContainerContentPresenter<V : UstadView>(override val context: Any,
                                                               override val arguments: Map<String, String>,
                                                               override val view: V):
        UstadBaseController<V>(context,arguments, view) {

    fun mountCounter(containerUid: Long): String {return ""}

    fun unMountContainer(containerUid: Long){}
}