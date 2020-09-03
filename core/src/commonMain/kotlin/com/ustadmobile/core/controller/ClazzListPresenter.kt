package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_INSERT
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ClazzListPresenter(context: Any, arguments: Map<String, String>, view: ClazzList2View,
                         di: DI, lifecycleOwner: DoorLifecycleOwner,
                         private val clazzList2ItemListener:
                         DefaultClazzListItemListener = DefaultClazzListItemListener(view, ListViewMode.BROWSER, context, di))
    : UstadListPresenter<ClazzList2View, Clazz>(context, arguments, view, di, lifecycleOwner), ClazzListItemListener by clazzList2ItemListener, OnSortOptionSelected, OnSearchSubmitted {

    var loggedInPersonUid = 0L

    private var filterExcludeMembersOfSchool: Long = 0

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        filterExcludeMembersOfSchool = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L
        clazzList2ItemListener.listViewMode = mListMode

        loggedInPersonUid = accountManager.activeAccount.personUid
        selectedSortOption = SORT_OPTIONS[0]
        updateList()
    }

    private fun updateList(searchText: String? = null) {
        view.list = repo.clazzDao.findClazzesWithPermission(if(searchText.isNullOrEmpty()) "%%" else "%${searchText}%",
                loggedInPersonUid, filterExcludeMembersOfSchool, selectedSortOption?.flag ?: 0)
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //All user should be able to see the plus button - but only those with permission can create a new class
        view.newClazzListOptionVisible = repo.clazzDao.personHasPermission(loggedInPersonUid, PERMISSION_CLAZZ_INSERT)
        return true
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(ClazzEdit2View.VIEW_NAME, mapOf(), context)
    }

    fun handleClickJoinClazz() {
        systemImpl.go(JoinWithCodeView.VIEW_NAME, mapOf(), context)
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateList()
    }

    override fun onSearchSubmitted(text: String?) {
        updateList(text)
    }


    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.name, ClazzDao.SORT_CLAZZNAME_ASC, true),
                SortOrderOption(MessageID.name, ClazzDao.SORT_CLAZZNAME_DESC, false),
                SortOrderOption(MessageID.attendance, ClazzDao.SORT_ATTENDANCE_ASC, true),
                SortOrderOption(MessageID.attendance, ClazzDao.SORT_ATTENDANCE_DESC, false)
        )
    }
}