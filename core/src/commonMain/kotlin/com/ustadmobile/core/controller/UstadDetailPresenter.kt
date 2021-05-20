package com.ustadmobile.core.controller

import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadDetailView
import com.ustadmobile.core.view.UstadSingleEntityView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

abstract class UstadDetailPresenter<V: UstadSingleEntityView<RT>, RT: Any>(context: Any,
     arguments: Map<String, String>, view: V, di: DI,
     lifecycleOwner: DoorLifecycleOwner)
    : UstadSingleEntityPresenter<V, RT>(context, arguments, view, di, lifecycleOwner) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        accountManager.activeAccountLive.observeWithLifecycleOwner(lifecycleOwner, this::onAccountChanged)
    }

    protected open fun onAccountChanged(account: UmAccount?) {
        val detailView  = (view as? UstadDetailView<*>) ?: return
        GlobalScope.launch(doorMainDispatcher()) {
            val canEdit = onCheckEditPermission(account)
            detailView.editButtonMode = if(canEdit)
                EditButtonMode.FAB
            else
                EditButtonMode.GONE
        }
    }

    abstract suspend fun onCheckEditPermission(account: UmAccount?): Boolean

    open fun handleClickEdit() {}

}