package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.RoleDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.RoleEditView
import com.ustadmobile.core.view.RoleListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class RoleListPresenter(context: Any, arguments: Map<String, String>, view: RoleListView,
                        di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<RoleListView, Role>(context, arguments, view, di, lifecycleOwner) {

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        selectedSortOption = SORT_OPTIONS[0]
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {

        return db.entityRoleDao.userHasAnySinglePermission(
                account?.personUid ?: 0, Role.PERMISSION_ROLE_INSERT)

    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }

    override fun onSearchSubmitted(text: String?) {
        updateListOnView(text)
    }

    private fun updateListOnView(searchText: String? = null) {
        view.list = repo.roleDao.findAllActiveRolesSorted(
                selectedSortOption?.flag ?: 0,
                if(searchText.isNullOrEmpty()) "%%" else "%${searchText}%")
    }


    override fun handleClickEntry(entry: Role) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(RoleEditView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to entry.roleUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(RoleEditView.VIEW_NAME, mapOf(), context)
    }


    fun handleRemoveRole(role:Role){
        GlobalScope.launch {
            role.roleActive = false
            repo.roleDao.updateAsync(role)
        }
    }

    companion object{
        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.name, RoleDao.SORT_NAME_ASC, true),
                SortOrderOption(MessageID.name, RoleDao.SORT_NAME_DESC, false)
        )
    }
}