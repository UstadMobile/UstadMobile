package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class SchoolListPresenter(context: Any, arguments: Map<String, String>, view: SchoolListView,
                          di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<SchoolListView, School>(context, arguments, view, di, lifecycleOwner),
        OnSortOptionSelected, OnSearchSubmitted{

    var searchText: String? = null
    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        selectedSortOption = SORT_OPTIONS[0]
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(account?.personUid ?: 0,
            School.TABLE_ID, Role.PERMISSION_SCHOOL_INSERT)
    }

    private fun updateListOnView() {
        view.list = repo.schoolDao.findAllActiveSchoolWithMemberCountAndLocationName(
                searchText.toQueryLikeParam(),
                selectedSortOption?.flag ?: SchoolDao.SORT_NAME_ASC)
    }

    override fun handleClickEntry(entry: School) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(SchoolDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.schoolUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(SchoolEditView.VIEW_NAME, mapOf(), context)
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }


    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.name, SchoolDao.SORT_NAME_ASC, true),
                SortOrderOption(MessageID.name, SchoolDao.SORT_NAME_DESC, false)
        )

    }

}