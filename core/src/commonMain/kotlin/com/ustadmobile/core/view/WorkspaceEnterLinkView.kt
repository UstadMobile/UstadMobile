package com.ustadmobile.core.view

interface WorkspaceEnterLinkView : UstadView {

    var workspaceLink:String?

    var validLink: Boolean

    var progressVisible: Boolean

    companion object {
        val VIEW_NAME = "WorkspaceEnterLinkView"
    }
}
