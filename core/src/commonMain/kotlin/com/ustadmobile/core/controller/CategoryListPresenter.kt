package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json

import org.kodein.di.DI

class CategoryListPresenter(context: Any, arguments: Map<String, String>, view: CategoryListView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<CategoryListView, Category>(context, arguments, view, di, lifecycleOwner),
        CategoryListItemListener {

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class CategoryListSortOption(val sortOrder: SortOrder, context: Any)
        : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { CategoryListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
//        TODO("check on add permission for this account: e.g. " +
//                "repo.clazzDao.personHasPermission(loggedInPersonUid, PERMISSION_CLAZZ_INSERT)")
        val person = withTimeoutOrNull(2000){
            repo.personDao.findByUidAsync(account?.personUid?: 0L)
        }?:Person()

        return person.admin
    }

    private fun updateListOnView() {
        //TODO :Add sorting if required
        view.list = repo.categoryDao.findAllActiveCategories()
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(CategoryEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? CategoryListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }

    override fun handleClickEntry(category: Category) {
        when(mListMode) {
            ListViewMode.PICKER -> {
                view.finishWithResult(listOf(category))
            }
            ListViewMode.BROWSER -> {
                systemImpl.go(CategoryEditView.VIEW_NAME,
                        mapOf(UstadView.ARG_ENTITY_UID to category.categoryUid.toString()), context)
            }
        }
    }

    override fun onClickCategory(category: Category) {
        when(mListMode) {
            ListViewMode.PICKER -> {
                view.finishWithResult(listOf(category))
            }
            ListViewMode.BROWSER -> {
                systemImpl.go(CategoryEditView.VIEW_NAME,
                        mapOf(UstadView.ARG_ENTITY_UID to category.categoryUid.toString()), context)
            }
        }
    }

    override fun onClickRemove(category: Category) {
        //TODO: this
    }
}