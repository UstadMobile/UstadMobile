package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.list.*
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.SelectionOption
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.model.statemanager.GlobalStateSlice
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.horizontalList
import com.ustadmobile.util.CssStyleManager.ustadListViewComponentContainer
import com.ustadmobile.util.CssStyleManager.listCreateNewContainer
import com.ustadmobile.util.CssStyleManager.listCreateNewLabel
import com.ustadmobile.util.CssStyleManager.listItemCreateNewDiv
import com.ustadmobile.util.StateManager
import kotlinx.css.RuleSet
import react.*
import styled.css
import styled.styledDiv
import styled.styledP

abstract class UstadListViewComponent<RT, DT>(mProps: RProps) : UmBaseComponent<RProps,RState>(mProps),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    private val stateChangeListener: (GlobalStateSlice) -> Unit
        get() = {
            if(it.state.view == ContentEntryList2View.VIEW_NAME){
                onComponentRefreshed()
            }
        }


    override fun componentDidMount() {
        StateManager.subscribe(stateChangeListener)
    }

    override fun RBuilder.render() {
        styledDiv {
            css(ustadListViewComponentContainer)
            mList {
                css{ +(styleList() ?: horizontalList) }
                mListItem {
                    css(listCreateNewContainer)
                    attrs {
                        alignItems = MListItemAlignItems.flexStart
                        button = true
                        divider = true
                        onClick = { listPresenter?.handleClickCreateNewFab() }
                    }
                    renderHeaderView()
                }

                getData(0,9).forEach { entry ->
                    mListItem {
                        attrs {
                            alignItems = MListItemAlignItems.flexStart
                            button = true
                            divider = true
                            onClick = { handleClickEntry(entry)} }
                        renderListItem(entry)
                    }
                }
            }
        }
    }

    open fun RBuilder.renderListItem(item: DT){}

    open fun RBuilder.renderHeaderView(){
        styledDiv {
            css(listItemCreateNewDiv)
            mListItemIcon("add","${CssStyleManager.name}-listCreateNewIcon")
            styledP {
                css(listCreateNewLabel)
                +systemImpl.getString(MessageID.add_new, this)
            }
        }
    }

    abstract fun handleClickEntry(entry: DT)

    abstract fun styleList(): RuleSet?

    abstract fun onComponentRefreshed()

    abstract fun getData(offset: Int, limit: Int): List<DT>

    override var list: DataSource.Factory<Int, DT>?
        get() = TODO("Not yet implemented")
        set(value) {}


    override var selectionOptions: List<SelectionOption>? = null
        get() = field
        set(value) {
            //Handle selection option stuffs here
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