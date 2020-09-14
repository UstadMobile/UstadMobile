package com.ustadmobile.core.view

interface InviteViaLinkView: UstadView {

    var inviteLink : String?
    var entityName : String?
    var inviteCode: String?

    companion object {

        const val VIEW_NAME = "InviteViaLink"

    }
}