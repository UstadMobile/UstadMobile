package com.ustadmobile.core.view

import com.ustadmobile.core.impl.UmCallback

/**
 * Created by mike on 1/6/18.
 */

interface ScormPackageView : UstadView {

    fun setTitle(title: String)

    fun loadUrl(url: String)

    fun mountZip(zipUri: String, callback: UmCallback<String>)

    companion object {

        const val VIEW_NAME = "ScormPackage"
    }

}
