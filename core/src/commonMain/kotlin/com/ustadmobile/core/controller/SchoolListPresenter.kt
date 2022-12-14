package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.core.db.dao.SchoolDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class SchoolListPresenter(context: Any, arguments: Map<String, String>, view: SchoolListView,
                          di: DI, lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<SchoolListView, School>(context, arguments, view, di, lifecycleOwner),
        OnSortOptionSelected, OnSearchSubmitted{


    var searchText: String? = null
    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var loggedInPersonUid = 0L
    private var filterByPermission: Long = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        selectedSortOption = SORT_OPTIONS[0]
        loggedInPersonUid = accountManager.activeAccount.personUid

        filterByPermission = arguments[UstadView.ARG_FILTER_BY_PERMISSION]?.toLong()
                ?: Role.PERMISSION_SCHOOL_SELECT

        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        view.newSchoolListOptionVisible =  db.entityRoleDao.userHasTableLevelPermission(account?.personUid ?: 0,
            Role.PERMISSION_SCHOOL_INSERT)
        return true
    }

    private fun updateListOnView() {
        view.list = repo.schoolDao.findAllActiveSchoolWithMemberCountAndLocationName(
                searchText.toQueryLikeParam(), loggedInPersonUid, filterByPermission,
                selectedSortOption?.flag ?: SchoolDaoCommon.SORT_NAME_ASC)
    }

    override fun handleClickEntry(entry: School) {
        when(mListMode) {
            ListViewMode.PICKER -> finishWithResult(safeStringify(di,
                ListSerializer(School.serializer()), listOf(entry)))
            ListViewMode.BROWSER -> systemImpl.go(SchoolDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.schoolUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(SchoolEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                SchoolEditView.VIEW_NAME,
                School::class,
                School.serializer(),
                destinationResultKey ?: SCHOOL_RESULT_KEY,
                arguments = args?.toMutableMap() ?: arguments.toMutableMap()
            )
        )
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }


    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }


    fun handleClickJoinSchool() {
        systemImpl.go(JoinWithCodeView.VIEW_NAME,
                mapOf(UstadView.ARG_CODE_TABLE to School.TABLE_ID.toString()), context)
    }

    companion object {

        const val SCHOOL_RESULT_KEY = "School"

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.name, SchoolDaoCommon.SORT_NAME_ASC, true),
                SortOrderOption(MessageID.name, SchoolDaoCommon.SORT_NAME_DESC, false)
        )

    }

}