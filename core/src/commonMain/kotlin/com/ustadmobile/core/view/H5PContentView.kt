package com.ustadmobile.core.view

/**
 * Created by mike on 2/15/18.
 */

interface H5PContentView : UstadView {

    fun setContentTitle(title: String)

    fun setContentHtml(baseUrl: String, html: String)

    companion object {

        const val VIEW_NAME = "H5PContentView"

        const val ANDROID_ASSETS_PATH = "/android-assets/"
    }


}
