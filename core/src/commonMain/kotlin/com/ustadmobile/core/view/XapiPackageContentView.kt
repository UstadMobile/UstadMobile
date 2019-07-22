package com.ustadmobile.core.view

/**
 * Created by mike on 9/13/17.
 */

interface XapiPackageContentView : UstadView, ViewWithErrorNotifier {

    fun setTitle(title: String)

    fun loadUrl(url: String)

    companion object {

        const val VIEW_NAME = "XapiPackage"

        const val ARG_CONTAINER_UID = "containerUid"
    }

}
