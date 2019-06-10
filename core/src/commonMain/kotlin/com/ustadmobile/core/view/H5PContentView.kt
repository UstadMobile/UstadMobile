package com.ustadmobile.core.view

import com.ustadmobile.core.impl.UmCallback

/**
 * Created by mike on 2/15/18.
 */

interface H5PContentView : UstadView {

    fun setTitle(title: String)

    fun setContentHtml(baseUrl: String, html: String)

    companion object {

        const val VIEW_NAME = "H5PContentView"
    }


}
