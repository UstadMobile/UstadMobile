package com.ustadmobile.core.view

import com.ustadmobile.core.impl.UmCallback

/**
 * Created by mike on 2/15/18.
 */

interface H5PContentView : UstadView {

    /**
     * Mount the h5p standalone dist directory, return the url to it
     * @return
     */
    fun mountH5PDist(callback: UmCallback<String>)

    fun mountH5PContainer(containerUid: Long, callback: UmCallback<String>)

    fun setTitle(title: String)

    fun setContentHtml(baseUrl: String, html: String)

    companion object {

        const val VIEW_NAME = "H5PContentView"
    }


}
