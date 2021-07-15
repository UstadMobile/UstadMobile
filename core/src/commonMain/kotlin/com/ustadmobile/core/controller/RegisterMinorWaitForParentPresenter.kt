package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.RegisterMinorWaitForParentView
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_PARENT_CONTACT
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_PASSWORD
import com.ustadmobile.core.view.RegisterMinorWaitForParentView.Companion.ARG_USERNAME
import com.ustadmobile.core.view.UstadView
import org.kodein.di.DI
import org.kodein.di.instance

class RegisterMinorWaitForParentPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: RegisterMinorWaitForParentView,
    di: DI
): UstadBaseController<RegisterMinorWaitForParentView>(
    context, arguments, view, di, activeSessionRequired = false
) {

    val systemImpl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.username = arguments[ARG_USERNAME] ?: "invalid"
        view.password = arguments[ARG_PASSWORD] ?: "invalid"
        view.parentContact = arguments[ARG_PARENT_CONTACT] ?: "invalid"
    }

    fun handleClickTogglePasswordVisibility() {
        view.passwordVisible = !view.passwordVisible
    }

    fun handleClickOk() {
        systemImpl.popBack(UstadView.CURRENT_DEST, true, context)
    }
}