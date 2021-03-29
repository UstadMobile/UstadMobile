package com.ustadmobile.core.view

interface SiteEnterLinkView : UstadView {

    var siteLink: String?

    var validLink: Boolean

    var progressVisible: Boolean

    companion object {
        val VIEW_NAME = "SiteEnterLinkView"
    }
}
