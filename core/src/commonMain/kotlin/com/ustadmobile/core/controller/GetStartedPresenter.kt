package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*

class GetStartedPresenter(context: Any, arguments: Map<String, String>?, view: AccountGetStartedView,
                          val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<AccountGetStartedView>(context, arguments!!, view) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    fun goToPublicLibrary(){

    }

    fun joinExistingWorkSpace(){
        impl.go(WorkspaceEnterLinkView.VIEW_NAME, arguments, context)
    }

    fun createNewWorkSpace(){
        view.createNewWorkSpace()
    }
}
