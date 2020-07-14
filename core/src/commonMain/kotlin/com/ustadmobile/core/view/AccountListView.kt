package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.UmAccount

interface AccountListView : UstadView {

    var accountListLive: DoorLiveData<List<UmAccount>>?

    var activeAccountLive: DoorLiveData<UmAccount>?

    fun showGetStarted()

    companion object {

        const val VIEW_NAME = "AccountListView"

    }

}
