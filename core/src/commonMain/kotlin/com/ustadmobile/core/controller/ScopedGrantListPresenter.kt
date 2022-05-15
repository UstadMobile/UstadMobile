package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI

class ScopedGrantListPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ScopedGrantListView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner,
    private val scopedGrantItemListener: DefaultScopedGrantListItemListener = DefaultScopedGrantListItemListener(view, ListViewMode.BROWSER, context, di)
): UstadListPresenter<ScopedGrantListView, ScopedGrant>(context, arguments, view, di, lifecycleOwner),
    ScopedGrantListItemListener by scopedGrantItemListener
{

    private var tableId = 0

    private var entityUid = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        tableId = arguments[ScopedGrantListView.ARG_FILTER_TABLE_ID]?.toInt() ?: 0
        entityUid = arguments[ScopedGrantListView.ARG_FILTER_ENTITY_UID]?.toLong() ?: 0L
        super.onCreate(savedState)

        updateListOnView()
        scopedGrantItemListener.listViewMode = mListMode
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return when(tableId) {
            Clazz.TABLE_ID -> repo.clazzDao.personHasPermissionWithClazz(account?.personUid ?: 0L,
                entityUid, Role.PERMISSION_PERSON_DELEGATE)
            else -> false
        }
    }

    private fun updateListOnView() {
        view.list = repo.scopedGrantDao.findByTableIdAndEntityUidWithNameAsDataSource(
            tableId,
            arguments[ScopedGrantListView.ARG_FILTER_ENTITY_UID]?.toLong() ?: 0L)
    }

    override fun handleClickCreateNewFab() {
        val args = mutableMapOf(
            ScopedGrantEditView.ARG_GRANT_ON_TABLE_ID to tableId.toString(),
            ScopedGrantEditView.ARG_GRANT_ON_ENTITY_UID to entityUid.toString(),
            UstadView.ARG_GO_TO_COMPLETE to ScopedGrantEditView.VIEW_NAME,
            UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())

        navigateForResult(NavigateForResultOptions(
            fromPresenter = this,
            currentEntityValue = null,
            destinationViewName = PersonListView.VIEW_NAME,
            entityClass = ScopedGrant::class,
            serializationStrategy = ScopedGrant.serializer(),
            arguments = args,
        ))
    }

}