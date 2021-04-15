package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.list.*
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.SelectionOption
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.model.statemanager.FabState
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.contentEntryListEditOptions
import com.ustadmobile.util.CssStyleManager.horizontalList
import com.ustadmobile.util.CssStyleManager.listCreateNewContainer
import com.ustadmobile.util.CssStyleManager.listItemCreateNewDiv
import com.ustadmobile.util.CssStyleManager.ustadListViewComponentContainer
import com.ustadmobile.util.StateManager
import kotlinx.css.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

abstract class UstadListViewComponent<RT, DT>(mProps: RProps) : UmBaseComponent<RProps,RState>(mProps),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    private lateinit var fabState: FabState

    protected var showCreateNewItem:Boolean = false

    override fun componentDidMount() {
        super.componentDidMount()
        fabState = FabState(label = systemImpl.getString(MessageID.content, this),
            icon = "add", onClick = ::onFabClick)
        searchManager?.searchListener = listPresenter
    }

    override fun RBuilder.render() {
        styledDiv {
            css(ustadListViewComponentContainer)

            styledDiv {
                css{
                    margin = "16px"
                    display = if(listFilterOptionChips.isNullOrEmpty())
                        Display.none else Display.block
                }
                if(!listFilterOptionChips.isNullOrEmpty()){
                    renderFilters()
                }
            }

            renderEditOptions()

            mList {
                css{ +(styleList() ?: horizontalList) }
                if(showCreateNewItem){
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
        renderAddEntryOptions()
    }

    open fun RBuilder.renderListItem(item: DT){}

    open fun RBuilder.renderHeaderView(){
        styledDiv {
            css(listItemCreateNewDiv)
            mListItemIcon("add","${CssStyleManager.name}-listCreateNewIcon")
            mTypography(variant = MTypographyVariant.button) {
                css{
                    marginTop = 3.px
                }
                +systemImpl.getString(MessageID.add_new, this)
            }
        }
    }

    private fun RBuilder.renderFilters(){
        styledDiv {
            css{ +CssStyleManager.chipSet}
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

    abstract fun handleClickEntry(entry: DT)

    abstract fun styleList(): RuleSet?


    abstract fun getData(offset: Int, limit: Int): List<DT>

    abstract fun RBuilder.renderAddEntryOptions()

    abstract fun RBuilder.renderEditOptions()

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

    override var sortOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            field = value
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