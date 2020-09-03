package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonListView.Companion.ARG_EXCLUDE_PERSONUIDS_LIST
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.getSystemTimeInMillis
import org.kodein.di.DI

class PersonListPresenter(context: Any, arguments: Map<String, String>, view: PersonListView,
                          di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<PersonListView, Person>(context, arguments, view, di, lifecycleOwner), OnSortOptionSelected, OnSearchSubmitted {

    private var filterExcludeMembersOfClazz: Long = 0

    private var filterExcludeMemberOfSchool: Long = 0

    private var filterAlreadySelectedList = listOf<Long>()

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterExcludeMembersOfClazz = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ]?.toLong() ?: 0L
        filterExcludeMemberOfSchool = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L
        filterAlreadySelectedList = arguments[ARG_EXCLUDE_PERSONUIDS_LIST]?.split(",")?.filter { it.isNotEmpty() }?.map { it.toLong() }
                ?: listOf()

        selectedSortOption = SORT_OPTIONS[0]
        updateListOnView()

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(account?.personUid ?: 0L,
                Person.TABLE_ID, Role.PERMISSION_PERSON_INSERT)
    }

    private fun updateListOnView(searchText: String? = null) {
        view.list = repo.personDao.findPersonsWithPermission(getSystemTimeInMillis(), filterExcludeMembersOfClazz,
                filterExcludeMemberOfSchool, filterAlreadySelectedList,
                accountManager.activeAccount.personUid, selectedSortOption?.flag ?: 0,
                if (searchText.isNullOrEmpty()) "%%" else "%${searchText}%")
    }

    override fun handleClickEntry(entry: Person) {
        when (mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(PersonDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.personUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(), context)
    }


    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }


    override fun onSearchSubmitted(text: String?) {
        updateListOnView(text)
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.first_name, PersonDao.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, PersonDao.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, PersonDao.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, PersonDao.SORT_LAST_NAME_DESC, false)
        )
    }
}