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
import com.ustadmobile.util.Util
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

    protected var showEmptyState = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    protected var showCreateNewItem:Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    var createNewText: String = getString(MessageID.add_new_content)
        get() = field
        set(value) {
            field = value
        }

    var inviteNewText: String = ""
        get() = field
        set(value) {
            field = value
        }

    var multiColumnItemSize = GridSize.cells4
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

            renderFilters()

            renderMenuOptions()

            renderHeaderView()

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
                            Util.stopEventPropagation(it)
                            handleClickCreateNewEntry()
                        }
                        createCreateNewItem(createNewText)
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
                umListItem( button = true, alignItems = ListItemAlignItems.flexStart) {
                    css(listCreateNewContainer)
                    attrs.divider = true
                    attrs.onClick = {
                        handleClickCreateNewEntry()
                    }
                    createCreateNewItem(createNewText)
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
                umItem(GridSize.cells12){
                    umGridContainer(GridSpacing.spacing4) {
                        dataListItems.forEach { entry->
                            umItem(GridSize.cells12, multiColumnItemSize){
                                css{
                                    cursor = Cursor.pointer
                                    backgroundColor = Color(if(selectedListItems.indexOf(entry) != -1)
                                        theme.palette.action.selected
                                    else theme.palette.background.default)
                                }
                                umPaper(elevation = 4) {
                                    styledDiv {
                                        renderListItem(entry)
                                    }

                                    attrs.onMouseDown = {
                                        stopEventPropagation(it)
                                        handleListItemPress(entry)
                                    }
                                    attrs.onMouseUp = {
                                        stopEventPropagation(it)
                                        handleListItemRelease(entry)
                                    }
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

    private fun RBuilder.renderFilters(){
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


    private fun RBuilder.renderMenuOptions(){
        val hideOptions = selectedListItems.isNullOrEmpty() or !showEditOptionsMenu
        umGridContainer(spacing = GridSpacing.spacing2) {
            css{
                +selectionContainer
                marginBottom = 2.spacingUnits
            }
            val titleMd = if(selectedListItems.isNullOrEmpty()) GridSize.cells11 else GridSize.cells7
            val titleMobile = if(selectedListItems.isNullOrEmpty()) GridSize.cells10 else GridSize.cells7
            val iconsMd = if(selectedListItems.isNullOrEmpty()) GridSize.cellsAuto else GridSize.cells5
            val iconsMobile = if(selectedListItems.isNullOrEmpty()) GridSize.cells1 else GridSize.cells5
            umItem(titleMobile, titleMd){
                umTypography(variant = TypographyVariant.subtitle1){
                    css {
                        marginTop = 10.px
                        marginLeft = 2.spacingUnits
                        display = displayProperty(!hideOptions)
                    }
                    +getString(MessageID.items_selected).format(selectedListItems.size)
                }
            }

            umItem(iconsMobile, iconsMd){
                umGridContainer(
                    spacing= GridSpacing.spacing2,
                    justify = GridJustify.flexEnd){
                    selectionOptions?.forEach { option ->
                        gridItem {
                            css {
                                display = displayProperty(!hideOptions)
                            }
                            umIconButton(SELECTION_ICONS_MAP[option]?:"delete",
                                color = UMColor.default){
                                attrs.onClick = {
                                    listPresenter?.handleClickSelectionOption(selectedListItems, option)
                                    setState {
                                        selectedListItems.clear()
                                    }
                                }
                            }
                        }
                    }
                    renderEditOptionMenu()
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

    open fun handleInviteClicked(){}

    open fun styleList(): RuleSet? {
        return null
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        //activate selected sort option
        listPresenter?.onClickSort(sortOption)
    }

    override fun finishWithResult(result: List<RT>) {
        TODO("finishWithResult: Not used anymore")
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
                SelectionOption.HIDE to "visibility",
                SelectionOption.UNHIDE to "visibility_of")

        val UI_LISTENER_TIMEOUT = 100
    }
}