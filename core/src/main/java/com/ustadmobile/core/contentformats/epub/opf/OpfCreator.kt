package com.ustadmobile.core.contentformats.epub.opf

/**
 * Created by mike on 12/12/17.
 */

class OpfCreator {

    var creator: String? = null

    var id: String? = null



    override fun toString(): String {
        return creator ?: super.toString()
    }
}
