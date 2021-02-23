package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonListView.Companion.ARG_EXCLUDE_PERSONUIDS_LIST
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI

class PersonListPresenter(context: Any, arguments: Map<String, String>, view: PersonListView,
                          di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<PersonListView, Person>(context, arguments, view, di, lifecycleOwner), OnSortOptionSelected, OnSearchSubmitted {

    private var filterExcludeMembersOfClazz: Long = 0

    private var filterExcludeMemberOfSchool: Long = 0

    private var filterAlreadySelectedList = listOf<Long>()

    private var filterByPermission: Long = 0

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterExcludeMembersOfClazz = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ]?.toLong() ?: 0L
        filterExcludeMemberOfSchool = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L
        filterAlreadySelectedList = arguments[ARG_EXCLUDE_PERSONUIDS_LIST]?.split(",")?.filter { it.isNotEmpty() }?.map { it.trim().toLong() }
                ?: listOf()

        filterByPermission = arguments[UstadView.ARG_FILTER_BY_PERMISSION]?.trim()?.toLong()
                ?: Role.PERMISSION_PERSON_SELECT

        selectedSortOption = SORT_OPTIONS[0]
        updateListOnView()

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(account?.personUid ?: 0L,
                Role.PERMISSION_PERSON_INSERT)
    }

    private fun updateListOnView() {
        view.list = repo.personDao.findPersonsWithPermission(getSystemTimeInMillis(), filterExcludeMembersOfClazz,
                filterExcludeMemberOfSchool, filterAlreadySelectedList,
                accountManager.activeAccount.personUid, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam())
    }

    override fun handleClickEntry(entry: Person) {
        when (mListMode) {
            ListViewMode.PICKER -> {

                if(arguments.containsKey(UstadView.ARG_GO_TO_COMPLETE)) {
                    systemImpl.go(arguments[UstadView.ARG_GO_TO_COMPLETE].toString(),
                            arguments.plus(UstadView.ARG_PERSON_UID to entry.personUid.toString()),
                            context)
                }else{
                    view.finishWithResult(listOf(entry))
                }
            }
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
        searchText = text
        updateListOnView()
    }


    fun handleClickInviteWithLink(){

        GlobalScope.launch {

            val code: String?
            val entityName: String?
            val tableId: Int
            when {
                filterExcludeMembersOfClazz != 0L -> {
                    val clazz = db.clazzDao.findByUidAsync(filterExcludeMembersOfClazz)
                    code = clazz?.clazzCode
                    entityName = clazz?.clazzName
                    tableId = Clazz.TABLE_ID
                }
                filterExcludeMemberOfSchool != 0L -> {
                    val school = db.schoolDao.findByUidAsync(filterExcludeMemberOfSchool)
                    code = school?.schoolCode
                    entityName = school?.schoolName
                    tableId = School.TABLE_ID
                }
                else -> {
                    code = ""
                    entityName = ""
                    tableId = 0
                }
            }

            view.runOnUiThread(Runnable {
                systemImpl.go(InviteViaLinkView.VIEW_NAME, mapOf(
                        UstadView.ARG_CODE_TABLE to tableId.toString(),
                        UstadView.ARG_CODE to code,
                        UstadView.ARG_ENTITY_NAME to entityName
                ), context)
            })
        }

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