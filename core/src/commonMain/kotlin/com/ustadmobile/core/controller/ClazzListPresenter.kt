package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_INSERT
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ClazzListPresenter(context: Any, arguments: Map<String, String>, view: ClazzList2View,
                         di: DI, lifecycleOwner: DoorLifecycleOwner,
                         private val clazzList2ItemListener:
                         DefaultClazzListItemListener = DefaultClazzListItemListener(view, ListViewMode.BROWSER, context, arguments, di))
    : UstadListPresenter<ClazzList2View, Clazz>(context, arguments, view, di, lifecycleOwner), ClazzListItemListener by clazzList2ItemListener, OnSortOptionSelected, OnSearchSubmitted {

    var loggedInPersonUid = 0L

    private var filterExcludeMembersOfSchool: Long = 0

    private var filterAlreadySelectedList = listOf<Long>()

    private var filterByPermission: Long = 0

    private var searchText: String? = null

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        filterExcludeMembersOfSchool = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L
        filterAlreadySelectedList = arguments[ClazzList2View.ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST]
                ?.split(",")?.filter { it.isNotEmpty() }?.map { it.trim().toLong() }
                ?: listOf()

        clazzList2ItemListener.listViewMode = mListMode

        filterByPermission = arguments[UstadView.ARG_FILTER_BY_PERMISSION]?.toLong()
                ?: Role.PERMISSION_CLAZZ_SELECT

        loggedInPersonUid = accountManager.activeAccount.personUid
        selectedSortOption = SORT_OPTIONS[0]

        view.listFilterOptionChips = FILTER_OPTIONS.toListFilterOptions(context, di)

        updateList()
    }

    private fun updateList() {
        view.list = repo.clazzDao.findClazzesWithPermission(
                searchText.toQueryLikeParam(),
                loggedInPersonUid, filterAlreadySelectedList,
                filterExcludeMembersOfSchool, selectedSortOption?.flag ?: 0,
                view.checkedFilterOptionChip?.optionId ?: 0,
                systemTimeInMillis(), filterByPermission, 0)
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //All user should be able to see the plus button - but only those with permission can create a new class
        view.newClazzListOptionVisible = repo.entityRoleDao.userHasTableLevelPermission(
                loggedInPersonUid, PERMISSION_CLAZZ_INSERT)
        return true
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(ClazzEdit2View.VIEW_NAME, mapOf(), context)
    }

    fun handleClickJoinClazz() {
        systemImpl.go(JoinWithCodeView.VIEW_NAME, mapOf(UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString()), context)
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateList()
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateList()
    }

    override fun onListFilterOptionSelected(filterOptionId: ListFilterIdOption) {
        super.onListFilterOptionSelected(filterOptionId)
        updateList()
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.name, ClazzDao.SORT_CLAZZNAME_ASC, true),
                SortOrderOption(MessageID.name, ClazzDao.SORT_CLAZZNAME_DESC, false),
                SortOrderOption(MessageID.attendance, ClazzDao.SORT_ATTENDANCE_ASC, true),
                SortOrderOption(MessageID.attendance, ClazzDao.SORT_ATTENDANCE_DESC, false)
        )

        val FILTER_OPTIONS = listOf(MessageID.active_classes to ClazzDao.FILTER_ACTIVE_ONLY,
                MessageID.all to 0)
    }
}