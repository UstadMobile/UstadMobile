package com.ustadmobile.core.view

import com.ustadmobile.core.impl.UmCallback

/**
 * Created by mike on 9/13/17.
 */

interface XapiPackageContentView : UstadView, ViewWithErrorNotifier {

    fun setTitle(title: String)

    fun loadUrl(url: String)

    fun mountContainer(containerUid: Long, callback: UmCallback<String>?)

    companion object {

        val VIEW_NAME = "XapiPackage"

        val ARG_CONTAINER_UID = "containerUid"
    }

}
