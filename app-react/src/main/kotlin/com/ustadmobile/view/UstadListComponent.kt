package com.ustadmobile.view

import com.ustadmobile.EmptyList
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
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignCenterItems
import com.ustadmobile.util.StyleManager.centerContainer
import com.ustadmobile.util.StyleManager.chipSetFilter
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.StyleManager.horizontalListEmpty
import com.ustadmobile.util.StyleManager.listComponentContainer
import com.ustadmobile.util.StyleManager.listCreateNewContainer
import com.ustadmobile.util.StyleManager.selectionContainer
import com.ustadmobile.util.StyleManager.theme
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.createCreateNewItem
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.browser.window
import kotlinx.css.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledSpan

abstract class UstadListComponent<RT, DT>(props: UmProps) : UstadBaseComponent<UmProps,UmState>(props),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    private var isEventHandled = false

    private var selectedListItems: MutableList<DT> = concurrentSafeListOf()

    protected var dataListItems: List<DT> = concurrentSafeListOf()

    protected var showEditOptionsMenu:Boolean = false

    private var listItemPressTimer = -1

    protected var dbRepo: UmAppDatabase? = null

    /**
     * Flag which shows/hide empty state on a list
     */
    protected var showEmptyState = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    /**
     * Flag which shows and hide the ADD entry layout at the top of the list
     */
    protected var showCreateNewItem:Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    var addNewEntryText: String = getString(MessageID.add_new_content)
        get() = field
        set(value) {
            field = value
        }

    var inviteNewText: String = ""
        get() = field
        set(value) {
            field = value
        }

    /**
     * Determines number of columns to be used on GridLayout.
     * GridSize.cells12 = Will take the entire screen
     * columns = (12/preferred grid size)
     * i.e GridSize.cells4, will display 3 columns (12/4)
     */
    var multiColumnItemSize = GridSize.cells4
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    /**
     * Flag to indicated which host design to be used on Gridlayout.
     * TRUE = Use Card design
     * FALSE = Use plain design
     *
     * i.e it has no effect on LinearLayout when set.
     */
    var useCards = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    /**
     * Flag to indicate whether the list should be multi (Grid Layout)
     * or single column (Linear Layout)
     */
    protected var linearLayout: Boolean = true
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

    private val dataObserver = ObserverFnWrapper<List<DT>>{
        setState {
            dataListItems = it
        }
        onDataListLoaded()
    }


    override var list: DoorDataSourceFactory<Int, DT>? = null
        get() = field
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(dataObserver)
            liveData?.observe(this, dataObserver)
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
            setState {
                field = value
                showCreateNewItem = value == ListViewAddMode.FIRST_ITEM
            }
            window.setTimeout({
                fabManager?.visible = value == ListViewAddMode.FAB
            }, STATE_CHANGE_DELAY)
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

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.icon = "add"
        fabManager?.text = ""
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)
        window.setTimeout({
            searchManager?.searchListener = listPresenter
        }, UI_LISTENER_TIMEOUT)
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                if(linearLayout){
                    +listComponentContainer
                }
                +contentContainer
            }

            renderEntrySelectionMenuOptions()

            renderEntriesFilterOptions()

            styledDiv {
                css{
                    paddingBottom = 2.spacingUnits
                }

                renderHeaderView()
            }

            if(linearLayout)
                renderSingleColumnList()
            else
                renderMultiColumnList()

            renderFooterView()
        }

        //Render dialog UI to be shown when fab is clicked  (Bottom sheet replacement)
        renderAddContentOptionsDialog()
    }

    open fun onDataListLoaded(){}

    private fun RBuilder.renderEmptyList(){
      if(showEmptyState){
          umGridContainer {
              css (centerContainer)
              styledDiv {
                  css{
                      +alignCenterItems
                      width = LinearDimension("200px")
                  }
                  umIcon(emptyList.icon ?: "crop_free", className = "${StyleManager.name}-emptyListIcon")
                  umTypography(emptyList.text ?: getString(MessageID.nothing_here),
                      variant = TypographyVariant.h6,
                      align = TypographyAlign.center){
                      css {
                          marginTop = LinearDimension("20px")
                      }
                  }
              }
          }

          window.setTimeout({
              loading = false
          }, UI_LISTENER_TIMEOUT)
      }
    }

    private fun RBuilder.renderNewItem(multiColumn: Boolean = false){
       if(showCreateNewItem){
            if(multiColumn){
                umItem(GridSize.cells12) {
                    css{
                        +listCreateNewContainer
                        +horizontalList
                    }
                    umListItem(button = true) {
                        attrs.onClick = {
                            stopEventPropagation(it)
                            handleClickAddNewEntry()
                        }
                        createCreateNewItem(addNewEntryText)
                    }
                }


                if(inviteNewText.isNotEmpty()){
                    umItem(GridSize.cells12) {
                        css{
                            +listCreateNewContainer
                            +horizontalList
                        }
                        attrs.asDynamic().onClick = {
                            handleInviteClicked()
                        }

                        createCreateNewItem(inviteNewText)
                    }
                }
            }else {
                umListItem(
                    button = true,
                    alignItems = ListItemAlignItems.flexStart) {
                    css(listCreateNewContainer)
                    attrs.divider = true
                    attrs.onClick = {
                        stopEventPropagation(it)
                        handleClickAddNewEntry()
                    }
                    createCreateNewItem(addNewEntryText)
                }

                if(inviteNewText.isNotEmpty()){
                    umListItem( button = true, alignItems = ListItemAlignItems.flexStart) {
                        css(listCreateNewContainer)
                        attrs.divider = true
                        attrs.onClick = {
                            handleInviteClicked()
                        }
                        createCreateNewItem(inviteNewText, "")
                    }
                }
            }
        }
    }


    private fun RBuilder.renderMultiColumnList(){
       umGridContainer {

            renderNewItem()

            if(dataListItems.isEmpty()){
                renderEmptyList()
            }else {
                umGridContainer(GridSpacing.spacing4) {
                    dataListItems.forEach { entry->
                        umItem(GridSize.cells12, multiColumnItemSize){
                            css{
                                cursor = Cursor.pointer
                                backgroundColor = Color(if(selectedListItems.indexOf(entry) != -1)
                                    theme.palette.action.selected
                                else theme.palette.background.default)
                                +alignCenterItems
                                padding(1.spacingUnits)
                            }

                            attrs.onMouseDown = {
                                stopEventPropagation(it)
                                handleListItemPress(entry)
                            }

                            attrs.onMouseUp = {
                                stopEventPropagation(it)
                                handleListItemRelease(entry)
                            }

                            if(useCards){
                                umPaper(elevation = 4) {
                                    css{
                                        width = LinearDimension("97%")
                                    }
                                    renderListItem(entry)
                                }
                            }else {
                                umListItem(button = true){
                                    renderListItem(entry)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun RBuilder.renderSingleColumnList(){
       umList {
            css{ +(styleList() ?: if(dataListItems.isNotEmpty()) horizontalList else horizontalListEmpty) }

            renderNewItem(false)

            if(dataListItems.isEmpty()){
                renderEmptyList()
            }else {
                dataListItems.forEach { entry->
                    umListItem(button = true) {
                        css{
                            backgroundColor = Color(if(selectedListItems.indexOf(entry) != -1)
                                theme.palette.action.selected
                            else theme.palette.background.paper)
                            width = LinearDimension("100%")
                        }
                        attrs.asDynamic().alignItems = ListItemAlignItems.flexStart
                        attrs.divider = true
                        attrs.asDynamic().onMouseDown = {
                            handleListItemPress(entry)
                        }
                        attrs.asDynamic().onMouseUp = {
                            handleListItemRelease(entry)
                        }
                        renderListItem(entry)
                    }
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
                if(selectedListItems.isEmpty()){
                    handleClickEntry(entry)
                }else{
                    handleSelectedEntry(entry)
                }
            }
        }
        window.clearTimeout(listItemPressTimer)
    }

    private fun handleSelectedEntry(entry: DT){
        val exists = selectedListItems.indexOf(entry) != -1
        setState {
            if(exists){
                selectedListItems.remove(entry)
            }else{
                selectedListItems.add(entry)
            }
            listPresenter?.handleSelectionOptionChanged(selectedListItems)
        }
    }

    abstract fun RBuilder.renderListItem(item: DT)

    private fun RBuilder.renderEntriesFilterOptions(){
        if(listFilterOptionChips == null) return
        styledDiv {
            css{
                margin(2.spacingUnits)
                +chipSetFilter
            }
            listFilterOptionChips?.forEach { chip ->
                val mColor = if(chip == checkedFilterOptionChip)
                    if(isDarkModeActive()) ChipColor.secondary else ChipColor.primary
                else ChipColor.default
                umChip(chip.description,
                    color = mColor) {
                    css {
                        margin(1.spacingUnits)
                    }
                    attrs.onClick = {
                        setState {
                            checkedFilterOptionChip = chip
                        }
                        listPresenter?.onListFilterOptionSelected(chip)
                    }
                }
            }
            if(checkedFilterOptionChip == null)
                checkedFilterOptionChip = listFilterOptionChips?.firstOrNull()
        }
    }


    private fun RBuilder.renderEntrySelectionMenuOptions(){
        val hideOptions = selectedListItems.isNullOrEmpty() or !showEditOptionsMenu
        umGridContainer {
            css{
                +selectionContainer
            }

            umItem(GridSize.cells7, flexDirection = FlexDirection.row){
                if(selectedListItems.isNotEmpty()){
                    styledSpan{
                        css{
                            marginRight = 2.spacingUnits
                        }

                        umIconButton("close",
                            color = UMColor.default){
                            css{
                                marginTop = 4.px
                            }
                            attrs.onClick = {
                                stopEventPropagation(it)
                                setState {
                                    selectedListItems = mutableListOf()
                                }
                            }
                        }
                    }
                    styledSpan {
                        umTypography(variant = TypographyVariant.subtitle1){
                            css {
                                marginTop = 10.px
                                marginLeft = 2.spacingUnits
                                fontSize = LinearDimension("1.2em")
                                display = displayProperty(!hideOptions)
                            }
                            +getString(MessageID.items_selected).format(selectedListItems.size)
                        }
                    }
                }
            }

            umItem(GridSize.cells5, flexDirection = FlexDirection.rowReverse) {
               if(selectedListItems.isEmpty()){
                   styledSpan {
                       css{
                           marginLeft = 2.spacingUnits
                       }
                       renderEditOptionMenu()
                   }
               }

               if(selectedListItems.isNotEmpty()){
                   selectionOptions?.reversed()?.forEach { option ->
                       styledSpan {
                           css{
                               marginLeft = 2.spacingUnits
                           }
                           umIconButton(SELECTION_ICONS_MAP[option]?:"delete",
                               color = UMColor.default){
                               attrs.onClick = {
                                   stopEventPropagation(it)
                                   val selectedEntries = copyOnWriteListOf(selectedListItems)
                                   listPresenter?.handleClickSelectionOption(selectedEntries.first(), option)
                                   setState {
                                       selectedListItems.clear()
                                   }
                               }
                           }
                       }
                   }
               }
            }

        }
    }

    open fun RBuilder.renderAddContentOptionsDialog(){}

    open val emptyList: EmptyList = EmptyList()

    open fun RBuilder.renderEditOptionMenu(){}

    open fun RBuilder.renderHeaderView(){}

    open fun RBuilder.renderFooterView(){}

    abstract fun handleClickEntry(entry: DT)

    open fun handleClickCreateNewEntry(){
        listPresenter?.handleClickCreateNewFab()
    }

    open fun handleClickAddNewEntry(){
        listPresenter?.handleClickAddNewItem()
    }

    open fun handleInviteClicked(){}

    open fun styleList(): RuleSet? {
        return null
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        //activate selected sort option
        listPresenter?.onClickSort(sortOption)
    }

    override fun onFabClicked() {
        super.onFabClicked()
        handleClickCreateNewEntry()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbRepo = null
        listPresenter?.onDestroy()
        selectedListItems.clear()
    }


    companion object {
        val SELECTION_ICONS_MAP = mapOf(SelectionOption.EDIT to "edit",
                SelectionOption.DELETE to "delete",
                SelectionOption.MOVE to "drive_file_move",
                SelectionOption.HIDE to "visibility_off",
                SelectionOption.UNHIDE to "visibility")

        private const val UI_LISTENER_TIMEOUT = 100
    }
}