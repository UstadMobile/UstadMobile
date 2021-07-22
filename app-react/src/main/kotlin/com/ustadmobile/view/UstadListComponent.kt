package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.list.*
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.SelectionOption
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.util.StyleManager.chipSetFilter
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.StyleManager.listCreateNewContainer
import com.ustadmobile.util.StyleManager.selectionContainer
import com.ustadmobile.util.StyleManager.theme
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.renderCreateNewItemView
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

abstract class UstadListComponent<RT, DT>(mProps: RProps) : UstadBaseComponent<RProps,RState>(mProps),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    private var isEventHandled = false

    private var selectedEntries: MutableList<DT> = concurrentSafeListOf()

    private var listItems: List<DT> = concurrentSafeListOf()

    protected var showEditOptionsMenu:Boolean = false

    private var listItemPressTimer = -1

    protected var dbRepo: UmAppDatabase? = null

    protected var showCreateNewItem:Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    var createNewTextId: Int = MessageID.add_new_content
        get() = field
        set(value) {
            field = value
        }

    private var multiColumnItemSize = MGridSize.cells4
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    protected var listType: Int = LIST_TYPE_SINGLE_COLUMN
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

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
            setState {
                field = value
            }
        }

    override var sortOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            showCreateNewItem = value == ListViewAddMode.FIRST_ITEM
            if(value == ListViewAddMode.FAB){
                fabManager?.visible = value == ListViewAddMode.FAB
            }
            fabManager?.onClickListener = {
                onFabClicked()
            }
            setState {
                field = value
            }
        }

    override var listFilterOptionChips: List<ListFilterIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var checkedFilterOptionChip: ListFilterIdOption? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreate() {
        super.onCreate()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)
        window.setTimeout({
            searchManager?.searchListener = listPresenter
        }, 100)
    }

    override fun RBuilder.render() {
        val singleColumnList = listType == LIST_TYPE_SINGLE_COLUMN
        styledDiv {
            css{
                if(singleColumnList){
                    +listComponentContainer
                }
                +contentContainer
            }
            renderFilters()
            renderMenuOptions()

            renderHeaderView()

            if(singleColumnList)
                renderSingleColumnList()
            else
                renderMultiColumnList()

            renderFooterView()
        }

        //Render dialog UI to be shown when fab is clicked
        renderAddEntryOptionsDialog()
    }


    private fun RBuilder.renderMultiColumnList(){
        umGridContainer {
            if(showCreateNewItem){
                umItem(MGridSize.cells12) {
                    css(listCreateNewContainer)
                    attrs.alignItems = MGridAlignItems.flexStart
                    attrs.asDynamic().onClick = {
                        handleClickCreateNewEntry()
                    }
                    renderCreateNewItemView(getString(createNewTextId))
                }
            }

            umItem(MGridSize.cells12){
                umGridContainer(MGridSpacing.spacing4) {
                    listItems.forEach {entry->
                        umItem(MGridSize.cells12, multiColumnItemSize){
                            css{
                                cursor = Cursor.pointer
                                backgroundColor = Color(if(selectedEntries.indexOf(entry) != -1)
                                    theme.palette.action.selected
                                else theme.palette.background.default)
                            }
                            mPaper(elevation = 4) {
                                styledDiv {
                                    renderListItem(entry)
                                }

                                attrs.onMouseDown = {
                                    handleListItemPress(entry)
                                }
                                attrs.onMouseUp = {
                                    handleListItemRelease(entry)
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private fun RBuilder.renderSingleColumnList(){
        mList {
            css{ +(styleList() ?: horizontalList) }
            if(showCreateNewItem){
                mListItem {
                    css(listCreateNewContainer)
                    attrs.alignItems = MListItemAlignItems.flexStart
                    attrs.button = true
                    attrs.divider = true
                    attrs.onClick = {
                        handleClickCreateNewEntry()
                    }
                    renderCreateNewItemView(getString(createNewTextId))
                }
            }

            listItems.forEach {entry->
                mListItem {
                    css{
                        backgroundColor = Color(if(selectedEntries.indexOf(entry) != -1)
                            theme.palette.action.selected
                        else theme.palette.background.paper)
                        width = LinearDimension("100%")
                    }
                    attrs.alignItems = MListItemAlignItems.flexStart
                    attrs.button = true
                    attrs.divider = true
                    attrs.onMouseDown = {
                        handleListItemPress(entry)
                    }
                    attrs.onMouseUp = {
                        handleListItemRelease(entry)
                    }
                    renderListItem(entry)
                }
            }
        }
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

    abstract fun RBuilder.renderListItem(item: DT)

    private fun RBuilder.renderFilters(){
        if(listFilterOptionChips == null) return
        styledDiv {
            css{
                margin = "16px"
                +chipSetFilter
            }
            listFilterOptionChips?.forEach { chip ->
                val mColor = if(chip == checkedFilterOptionChip) MChipColor.primary
                else MChipColor.default
                mChip(chip.description,
                    color = mColor,
                    onClick = {
                        setState {
                            checkedFilterOptionChip = chip
                        }
                        listPresenter?.onListFilterOptionSelected(chip)
                    }) {
                    css {
                        margin(1.spacingUnits)
                    }
                }
            }
            if(checkedFilterOptionChip == null)
                checkedFilterOptionChip = listFilterOptionChips?.firstOrNull()
        }
    }

    private fun RBuilder.renderMenuOptions(){
        val hideOptions = selectedEntries.isNullOrEmpty() or !showEditOptionsMenu
        umGridContainer {
            css(selectionContainer)
            umItem(MGridSize.cells7, MGridSize.cells8){
                mTypography(variant = MTypographyVariant.subtitle1,
                    color = MTypographyColor.textPrimary){
                    css {
                        width = 10.pc
                        marginTop = 10.px
                        marginLeft = 16.px
                        display = displayProperty(!hideOptions)
                    }
                    +getString(MessageID.items_selected).format(selectedEntries.size)
                }
            }

            umItem(MGridSize.cells5, MGridSize.cells4){
                umGridContainer(spacing= MGridSpacing.spacing2, justify = MGridJustify.flexEnd){
                    selectionOptions?.forEach { option ->
                        mGridItem {
                            css {
                                display = displayProperty(!hideOptions)
                            }
                            mIconButton(SELECTION_ICONS_MAP[option]?:"delete",
                                color = MColor.default,
                                onClick = {
                                listPresenter?.handleClickSelectionOption(selectedEntries, option)
                                setState {
                                    selectedEntries.clear()
                                }
                            })
                        }
                    }
                    renderEditOptionMenu()
                }
            }

        }
    }

    open fun RBuilder.renderAddEntryOptionsDialog(){}

    open fun RBuilder.renderEditOptionMenu(){}

    open fun RBuilder.renderFooterView(){}

    open fun RBuilder.renderHeaderView(){}

    abstract fun handleClickEntry(entry: DT)

    open fun handleClickCreateNewEntry(){
        listPresenter?.handleClickCreateNewFab()
    }

    open fun styleList(): RuleSet? {
        return null
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        //activate selected sort option
        listPresenter?.onClickSort(sortOption)
    }

    override fun finishWithResult(result: List<RT>) {
        saveResultToBackStackSavedStateHandle(result)
    }

    override fun onFabClicked() {
        super.onFabClicked()
        handleClickCreateNewEntry()
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

        /**
         * List type on which list item cover the entire row
         */
        const val LIST_TYPE_SINGLE_COLUMN = 1

        /**
         * List type on which list item cover
         * just a fraction of space and it might have two or more columns
         */
        const val LIST_TYPE_MULTI_COLUMN = 2
    }
}