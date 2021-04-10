package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.list.*
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.SelectionOption
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.model.statemanager.UmFab
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmStyles
import com.ustadmobile.util.UmStyles.horizontalList
import com.ustadmobile.util.UmStyles.listContainer
import com.ustadmobile.util.UmStyles.listCreateNewContainer
import com.ustadmobile.util.UmStyles.listCreateNewLabel
import com.ustadmobile.util.UmStyles.listItemCreateNewDiv
import kotlinx.browser.window
import kotlinx.css.RuleSet
import react.RBuilder
import react.RProps
import react.RState
import react.ReactElement
import styled.css
import styled.styledDiv
import styled.styledP

abstract class UstadListViewComponent<RT, DT>(mProps: RProps) : UmBaseComponent<RProps,RState>(mProps),
    UstadListView<RT, DT>, OnSortOptionSelected {

    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    private var viewContainerToBeRendered: ReactElement? = null

    override fun RBuilder.render() {
        viewContainerToBeRendered
    }

    protected fun RBuilder.renderViews(){
        viewContainerToBeRendered = styledDiv {
            css(listContainer)
            mList {
                css(styleList() ?: horizontalList)
                mListItem {
                    css(listCreateNewContainer)
                    attrs {
                        alignItems = MListItemAlignItems.flexStart
                        button = true
                        divider = true
                        onClick = {
                            StateManager.dispatch(UmFab(showFab = true, isDetailScreen = false))
                            listPresenter?.handleClickCreateNewFab() }
                    }
                    renderHeaderView() ?: styledDiv {
                        css(listItemCreateNewDiv)
                        mListItemIcon("add","${UmStyles.name}-listCreateNewIcon")
                        styledP {
                            css(listCreateNewLabel)
                            +systemImpl.getString(MessageID.add_new, this)
                        }
                    }
                }
                getData(0,9).forEach { entry ->
                    mListItem {
                        attrs {
                            alignItems = MListItemAlignItems.flexStart
                            button = true
                            divider = true
                            onClick = {
                                handleClickEntry(entry)}
                        }
                        renderListItem(entry)
                    }
                }
            }
        }
    }


    abstract fun RBuilder.renderListItem(item: DT): MutableList<ReactElement>?

    abstract fun RBuilder.renderHeaderView(): ReactElement?

    abstract fun handleClickEntry(entry: DT)

    abstract fun styleList(): RuleSet?

    override var list: DataSource.Factory<Int, DT>?
        get() = TODO("Not yet implemented")
        set(value) {}

    fun getData(offset: Int, limit: Int): List<DT> {
        return window.asDynamic().testData as List<DT>
    }

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