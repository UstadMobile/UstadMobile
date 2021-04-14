package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.MChipColor
import com.ccfraser.muirwik.components.list.*
import com.ccfraser.muirwik.components.mChip
import com.ccfraser.muirwik.components.spacingUnits
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.*
import com.ustadmobile.model.statemanager.FabState
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.horizontalList
import com.ustadmobile.util.CssStyleManager.ustadListViewComponentContainer
import com.ustadmobile.util.CssStyleManager.listCreateNewContainer
import com.ustadmobile.util.CssStyleManager.listCreateNewLabel
import com.ustadmobile.util.CssStyleManager.listItemCreateNewDiv
import com.ustadmobile.util.StateManager
import kotlinx.css.RuleSet
import kotlinx.css.margin
import org.w3c.dom.events.Event
import react.*
import styled.css
import styled.styledDiv
import styled.styledP

abstract class UstadListViewComponent<RT, DT>(mProps: RProps) : UmBaseComponent<RProps,RState>(mProps),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    private lateinit var fabState: FabState

    private var showCreateNewItem:Boolean = false

    override fun componentDidMount() {
        fabState = FabState(label = systemImpl.getString(MessageID.content, this),
            icon = "add", onClick = ::onFabClick)
    }

    override fun RBuilder.render() {
        styledDiv {
            css(ustadListViewComponentContainer)

            styledDiv {
                css{
                    margin = "16px"
                }

                renderFilters()
            }

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
                    if(showCreateNewItem){
                        renderHeaderView()
                    }
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

    private fun RBuilder.renderFilters(){
        if(!listFilterOptionChips.isNullOrEmpty()){
            styledDiv {
                css{ CssStyleManager.chipSet }
                listFilterOptionChips?.forEach { chip ->
                    val mColor = if(chip == checkedFilterOptionChip) MChipColor.primary
                    else MChipColor.default
                    mChip(chip.description, color = mColor,onClick = {
                        setState { checkedFilterOptionChip = chip }
                        listPresenter?.onListFilterOptionSelected(chip)
                    }) {
                        css { margin(1.spacingUnits) }
                    }
                }
                if(checkedFilterOptionChip == null)
                    checkedFilterOptionChip = listFilterOptionChips?.firstOrNull()
            }
        }
    }

    abstract fun handleClickEntry(entry: DT)

    abstract fun styleList(): RuleSet?


    abstract fun getData(offset: Int, limit: Int): List<DT>

    override var list: DataSource.Factory<Int, DT>? = null
        get() = field
        set(value) {
            field = value
        }


    override var selectionOptions: List<SelectionOption>? = null
        get() = field
        set(value) {
            //Handle selection option stuffs here
            setState { field = value }
        }

    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            //Handle adding mode stufss here
            showCreateNewItem = value == ListViewAddMode.FIRST_ITEM
            if(value == ListViewAddMode.FAB){
                fabState.visible = value == ListViewAddMode.FAB
                StateManager.dispatch(fabState)
            }
            field = value
        }

    override var listFilterOptionChips: List<ListFilterIdOption>? = null
        get() = field
        set(value) {
            //handle filter options here
            setState {
                field = value
            }
        }

    override var checkedFilterOptionChip: ListFilterIdOption? = null
        get() = field
        set(value) {
            setState { field = value }
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

    private fun onFabClick(event: Event){
        listPresenter?.handleClickCreateNewFab()
    }

    companion object {

        val SELECTION_ICONS_MAP = mapOf(SelectionOption.EDIT to "edit",
                SelectionOption.DELETE to "delete",
                SelectionOption.MOVE to "drive_file_move",
                SelectionOption.HIDE to "visibility",
                SelectionOption.UNHIDE to "visibility_of")
    }
}