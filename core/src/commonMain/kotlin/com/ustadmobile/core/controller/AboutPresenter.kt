package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AboutView
import kotlinx.datetime.Instant
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * Created by mike on 12/27/16.
 */
class AboutPresenter(context: Any, args: Map<String, String>?, view: AboutView,
                     di: DI)
    : UstadBaseController<AboutView>(context, args!!, view, di) {

    val impl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.setVersionInfo(impl.getVersion(context) + " - " +
            Instant.fromEpochMilliseconds(impl.getBuildTimestamp(context)).toString())
    }

}
