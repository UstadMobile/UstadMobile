package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.UmAccount

interface AccountListView : UstadView {

    var storedAccounts: List<UmAccount>?

    var activeAccount: UmAccount?

    companion object {

        const val VIEW_NAME = "AccountListView"

    }

}
