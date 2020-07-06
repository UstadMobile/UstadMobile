package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import org.kodein.di.DI
import org.kodein.di.instance

class GetStartedPresenter(context: Any, arguments: Map<String, String>, view: GetStartedView,
                          di: DI) :
        UstadBaseController<GetStartedView>(context, arguments, view, di) {

    private val impl: UstadMobileSystemImpl by instance()

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
