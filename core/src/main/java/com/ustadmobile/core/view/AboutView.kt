package com.ustadmobile.core.view

/**
 * Created by mike on 12/27/16.
 */

interface AboutView : UstadView {

    fun setVersionInfo(versionInfo: String)

    fun setAboutHTML(aboutHTML: String)

    companion object {

        val VIEW_NAME = "About"
    }
}
