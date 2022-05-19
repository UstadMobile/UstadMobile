package com.ustadmobile.core.controller

import com.ustadmobile.core.view.HtmlTextViewDetailView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI


class HtmlTextViewDetailPresenter(context: Any,
                                  arguments: Map<String, String>, view: HtmlTextViewDetailView,
                                  lifecycleOwner: DoorLifecycleOwner,
                                  di: DI)
    : UstadDetailPresenter<HtmlTextViewDetailView, String>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override fun onLoadFromJson(bundle: Map<String, String>): String? {
        super.onLoadFromJson(bundle)

        val description = bundle[HtmlTextViewDetailView.DISPLAY_TEXT]
        val title = bundle[HtmlTextViewDetailView.DISPLAY_TITLE]

        view.title = title

        return description
    }

    companion object {

    }

}