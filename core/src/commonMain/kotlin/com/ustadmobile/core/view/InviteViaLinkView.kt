package com.ustadmobile.core.view

interface InviteViaLinkView: UstadView {

    var inviteLink : String?
    var entityName : String?

    companion object {

        const val VIEW_NAME = "InviteViaLink"

    }
}