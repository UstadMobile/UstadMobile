package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.view.NetworkNodeListView
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class NetworkNodeListPresenter(context: Any, args: Map<String, String>, view: NetworkNodeListView,
    di: DI) : UstadBaseController<NetworkNodeListView>(context, args, view, di) {

    val accountManager: UstadAccountManager by di.instance()

    val localAvailabilityManager: LocalAvailabilityManager by di.on(accountManager.activeAccount).instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.deviceList = localAvailabilityManager.networkNodesLiveData
    }

}