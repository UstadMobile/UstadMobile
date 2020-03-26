package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class UstadEditPresenter<V: UstadEditView<RT>, RT>(context: Any,
    arguments: Map<String, String>, view: V,
    lifecycleOwner: DoorLifecycleOwner,
    systemImpl: UstadMobileSystemImpl,
    db: UmAppDatabase,
    repo: UmAppDatabase,
    activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadSingleEntityPresenter<V, RT>(context, arguments, view, lifecycleOwner, systemImpl, db, repo, activeAccount) {

    abstract fun handleClickSave(entity: RT)

}