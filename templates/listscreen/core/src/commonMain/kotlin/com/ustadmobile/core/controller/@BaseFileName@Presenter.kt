package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class @BaseFileName@Presenter(
    context: Any,
    arguments: Map<String, String>,
    view: @BaseFileName@View,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner,
    private val @Entity_VariableName@ItemListener: Default@BaseFileName@ItemListener = Default@BaseFileName@ItemListener(view, ListViewMode.BROWSER, context, di)
) : UstadListPresenter<@BaseFileName@View, @Entity@>(context, arguments, view, di, lifecycleOwner),
    @BaseFileName@ItemListener by @Entity_VariableName@ItemListener
{

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        @Entity_VariableName@ItemListener.listViewMode = mListMode
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        TODO("check on add permission for this account: e.g. " +
                "repo.clazzDao.personHasPermission(loggedInPersonUid, PERMISSION_CLAZZ_INSERT)")
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = repo.daoName.findAllActiveClazzesSortByNameAsc(searchQuery, loggedInPersonUid, sortFlag)
        */
    }

    override fun handleClickCreateNewFab() {
        /* TODO: Add code to go to the edit view when the user clicks the new item FAB. This is only
         * called when the fab is clicked, not if the first item is create new item (e.g. picker mode).
         * That has to be handled at a platform level to use prepareCall etc.
        systemImpl.go(@Entity@EditView.VIEW_NAME, mapOf(), context)
         */
    }

}