package com.ustadmobile.core.controller

import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadDetailView
import com.ustadmobile.core.view.UstadSingleEntityView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.launch
import org.kodein.di.DI

abstract class UstadDetailPresenter<V: UstadSingleEntityView<RT>, RT: Any>(
    context: Any,
    arguments: Map<String, String>,
    view: V,
    di: DI,
    lifecycleOwner: LifecycleOwner,
    activeSessionRequired: Boolean = true
) : UstadSingleEntityPresenter<V, RT>(
    context, arguments, view, di, lifecycleOwner, activeSessionRequired
) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        updateFabDisplay()
    }

    protected open fun updateFabDisplay() {
        val detailView  = (view as? UstadDetailView<*>) ?: return
        presenterScope.launch {
            val canEdit = onCheckEditPermission(accountManager.activeAccount)
            detailView.editButtonMode = if(canEdit)
                EditButtonMode.FAB
            else
                EditButtonMode.GONE
        }
    }

    abstract suspend fun onCheckEditPermission(account: UmAccount?): Boolean

    open fun handleClickEdit() {}

}