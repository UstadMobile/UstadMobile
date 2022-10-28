package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.view.GrantAppPermissionView
import org.kodein.di.DI

class GrantAppPermissionPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: GrantAppPermissionView,
    di: DI,
): UstadBaseController<GrantAppPermissionView>(
    context, arguments, view, di
) {

    fun onClickApprove(session: UserSessionWithPersonAndEndpoint) {

    }

}