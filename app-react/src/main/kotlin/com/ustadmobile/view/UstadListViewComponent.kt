package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mIconButton
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
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.model.statemanager.FabState
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.chipSet
import com.ustadmobile.util.CssStyleManager.contentEntryListExtraOptions
import com.ustadmobile.util.CssStyleManager.horizontalList
import com.ustadmobile.util.CssStyleManager.listCreateNewContainer
import com.ustadmobile.util.CssStyleManager.listItemCreateNewDiv
import com.ustadmobile.util.CssStyleManager.ustadListViewComponentContainer
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.ext.format
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

abstract class UstadListViewComponent<RT, DT>(mProps: RProps) : UstadBaseComponent<RProps,RState>(mProps),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    private lateinit var fabState: FabState

    private var showCreateNewItem:Boolean = false

    private var isEventHandled = false

    private var selectedEntries: MutableList<DT> = concurrentSafeListOf()

    private var listItems: List<DT> = concurrentSafeListOf()

    protected var showEditOptionsMenu:Boolean = false

    private var listItemPressTimer = -1

    private val itemPressEventHandler:(Event) -> Unit  = {
        if(!isEventHandled){
            //Long press detected, add to selection
            handleSelectedEntry(it.asDynamic() as DT)
        }
        isEventHandled = true
    }


    override var list: DataSource.Factory<Int, DT>? = null
        get() = field
        set(value) {
            GlobalScope.launch(Dispatchers.Main) {
                val items = value?.getData(0,0)
                setState {
                    listItems = items?: concurrentSafeListOf()
                }
            }
            field = value
        }


    override var selectionOptions: List<SelectionOption>? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var sortOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            showCreateNewItem = value == ListViewAddMode.FIRST_ITEM
            if(value == ListViewAddMode.FAB){
                fabState.visible = value == ListViewAddMode.FAB
                StateManager.dispatch(fabState)
            }
            setState { field = value }
        }

    override var listFilterOptionChips: List<ListFilterIdOption>? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var checkedFilterOptionChip: ListFilterIdOption? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override fun componentDidMount() {
        super.componentDidMount()
        fabState = FabState(label = systemImpl.getString(MessageID.content, this),
            icon = "add", onClick = ::onFabClick)
        searchManager?.searchListener = listPresenter
    }

    override fun RBuilder.render() {
        styledDiv {
            css(ustadListViewComponentContainer)
            renderFilters()
            renderMenuOptions()

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

                listItems.forEach {entry->
                    mListItem {
                        css{
                            backgroundColor = Color(if(selectedEntries.indexOf(entry) != -1)
                                umTheme.theme.palette.action.selected
                            else umTheme.theme.palette.background.paper)
                        }
                        attrs {
                            alignItems = MListItemAlignItems.flexStart
                            button = true
                            divider = true
                            onMouseDown = {handleListItemPress(entry)}
                            onMouseUp = {handleListItemRelease(entry)}
                        }
                        renderListItem(entry)
                    }
                }
            }
        }
        renderAddEntryOptions()
    }


    private fun handleListItemPress(entry: DT){
        if(listItemPressTimer != -1) window.clearTimeout(listItemPressTimer)
        listItemPressTimer = window.setTimeout(itemPressEventHandler,1000,entry)
        isEventHandled = false
    }

    private fun handleListItemRelease(entry: DT){
        if(!showEditOptionsMenu){
            handleClickEntry(entry)
        }else{
            if(!isEventHandled){
                isEventHandled = true
                if(selectedEntries.isEmpty()){
                    handleClickEntry(entry)
                }else{
                    handleSelectedEntry(entry)
                }
            }
        }
        window.clearTimeout(listItemPressTimer)
    }

    private fun handleSelectedEntry(entry: DT){
        val exists = selectedEntries.indexOf(entry) != -1
        setState {
            if(exists){
                selectedEntries.remove(entry)
            }else{
                selectedEntries.add(entry)
            }
            listPresenter?.handleSelectionOptionChanged(selectedEntries)
        }
    }


    open fun RBuilder.renderListItem(item: DT){}

    open fun RBuilder.renderHeaderView(){
        styledDiv {
            css(listItemCreateNewDiv)
            mListItemIcon("add","${CssStyleManager.name}-listCreateNewIcon")
            mTypography(variant = MTypographyVariant.button) {
                css{ marginTop = 3.px }
                +systemImpl.getString(MessageID.add_new, this)
            }
        }
    }

    private fun RBuilder.renderFilters(){
        if(listFilterOptionChips == null) return
        styledDiv {
            css{ margin = "16px" }
            styledDiv {
                css{ +chipSet}
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

    private fun RBuilder.renderMenuOptions(){
        val hideOptions = selectedEntries.isNullOrEmpty() or !showEditOptionsMenu
        styledDiv {
            css{
                +contentEntryListExtraOptions
            }
            mTypography(variant = MTypographyVariant.subtitle1){
                css {
                    width = 10.pc
                    marginTop = 10.px
                    marginLeft = 16.px
                    display = if(hideOptions) Display.none else Display.block
                }
                +systemImpl.getString(MessageID.items_selected, this).format(selectedEntries.size)
            }
            mGridContainer(spacing= MGridSpacing.spacing2, justify = MGridJustify.flexEnd){
                selectionOptions?.forEach { option ->
                    mGridItem {
                        css { display = if(hideOptions) Display.none else Display.block}
                        mIconButton(SELECTION_ICONS_MAP[option]?:"delete", color = MColor.default,onClick = {
                            listPresenter?.handleClickSelectionOption(selectedEntries, option)
                            setState { selectedEntries.clear()}
                        })
                    }
                }
                renderEditOptionMenu()
            }
        }
    }

    abstract fun RBuilder.renderAddEntryOptions()

    abstract fun RBuilder.renderEditOptionMenu()

    abstract fun handleClickEntry(entry: DT)

    abstract fun styleList(): RuleSet?

    override fun onClickSort(sortOption: SortOrderOption) {
        //activate selected sort option
        listPresenter?.onClickSort(sortOption)
    }

    override fun finishWithResult(result: List<RT>) {
        //Save result to back stack here
    }

    private fun onFabClick(event: Event){
        listPresenter?.handleClickCreateNewFab()
    }

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        selectedEntries.clear()
    }


    companion object {
        val SELECTION_ICONS_MAP = mapOf(SelectionOption.EDIT to "edit",
                SelectionOption.DELETE to "delete",
                SelectionOption.MOVE to "drive_file_move",
                SelectionOption.HIDE to "visibility",
                SelectionOption.UNHIDE to "visibility_of")
    }
}