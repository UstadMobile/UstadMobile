package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*

class WorkspaceEnterLinkPresenter(context: Any, arguments: Map<String, String>, view: WorkspaceEnterLinkView,
                                  val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<WorkspaceEnterLinkView>(context, arguments, view) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

}
