package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.UmAccount

interface AccountListView : UstadView {

    var accountListLive: DoorLiveData<List<UmAccount>>?

    var activeAccountLive: DoorLiveData<UmAccount>?

    fun showContentEntryList(account: UmAccount)

    companion object {

        const val VIEW_NAME = "AccountListView"

        const val ARG_FILTER_BY_ENDPOINT = "filterByEndpoint"

    }

}
