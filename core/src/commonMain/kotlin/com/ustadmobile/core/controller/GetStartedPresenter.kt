package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*

class GetStartedPresenter(context: Any, arguments: Map<String, String>?, view: GetStartedView,
                          val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<GetStartedView>(context, arguments!!, view) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    fun goToPublicLibrary(){
        impl.go(Login2View.VIEW_NAME, arguments, context)
    }

    fun joinExistingWorkSpace(){
        impl.go(WorkspaceEnterLinkView.VIEW_NAME, arguments, context)
    }

    fun createNewWorkSpace(){
        view.createNewWorkSpace()
    }
}
