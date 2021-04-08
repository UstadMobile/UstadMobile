package com.ustadmobile.view

import androidx.paging.DataSource
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.SelectionOption
import com.ustadmobile.core.view.UstadListView
import react.RBuilder
import react.RProps
import react.RState

abstract class UstadListViewComponent<RT, DT>(mProps: RProps) : UmBaseComponent<RProps,RState>(mProps),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    override fun RBuilder.render() {
        TODO("Not yet implemented")
    }

    override var list: DataSource.Factory<Int, DT>?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var selectionOptions: List<SelectionOption>? = null
        get() = field
        set(value) {
            //Handle section option stuffs here
            field = value
        }
    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            //Handle adding mode stufss here
            field = value
        }

    override var listFilterOptionChips: List<ListFilterIdOption>? = null
        set(value) {
            //handle filter options here
            field = value
        }

    override var checkedFilterOptionChip: ListFilterIdOption? = null
        get() = field
        set(value) {
            field = value
            //handle checked filter options here
        }

    override fun onClickSort(sortOption: SortOrderOption) {
        //activate selected sort option
        listPresenter?.onClickSort(sortOption)
    }

    override fun finishWithResult(result: List<RT>) {
        //Save result to back stack here
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        //handle showing snackbar
    }

    companion object {

        val SELECTION_ICONS_MAP =
            mapOf(SelectionOption.EDIT to "edit",
                SelectionOption.DELETE to "delete",
                SelectionOption.MOVE to "drive_file_move",
                SelectionOption.HIDE to "visibility",
                SelectionOption.UNHIDE to "visibility_of")
    }
}